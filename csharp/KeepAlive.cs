using System.Text.RegularExpressions;
using MongoDB.Bson;
using MongoDB.Driver;

namespace MongoKeepAlive;

/// <summary>
/// Keep MongoDB Atlas free-tier clusters alive by periodically sending a ping command.
/// </summary>
public sealed class KeepAlive : IDisposable
{
    private const string DefaultInterval = "12h";
    private const int MaxRetries = 3;
    private static readonly TimeSpan RetryDelay = TimeSpan.FromSeconds(5);

    private readonly IMongoClient _client;
    private readonly CancellationTokenSource _cts;
    private readonly Task _task;

    private KeepAlive(IMongoClient client, CancellationTokenSource cts, Task task)
    {
        _client = client;
        _cts = cts;
        _task = task;
    }

    /// <summary>
    /// Start the keep-alive loop with the default interval (12h).
    /// </summary>
    public static KeepAlive StartKeepAlive(string uri) => StartKeepAlive(uri, DefaultInterval);

    /// <summary>
    /// Start the keep-alive loop.
    /// </summary>
    /// <param name="uri">MongoDB connection string.</param>
    /// <param name="interval">Ping interval, e.g. "12h", "30m", "60s".</param>
    /// <returns>A <see cref="KeepAlive"/> handle. Dispose it to stop.</returns>
    public static KeepAlive StartKeepAlive(string uri, string interval)
    {
        if (string.IsNullOrWhiteSpace(uri))
            throw new ArgumentException("A MongoDB connection URI is required.", nameof(uri));

        if (string.IsNullOrWhiteSpace(interval))
            interval = DefaultInterval;

        var intervalTs = ParseInterval(interval);
        Log($"Starting with interval {interval} ({intervalTs.TotalMilliseconds} ms)");

        var client = new MongoClient(uri);
        var db = client.GetDatabase("admin");

        // Initial ping
        PingWithRetry(db);

        var cts = new CancellationTokenSource();
        var token = cts.Token;

        var task = Task.Run(async () =>
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(intervalTs, token);
                    PingWithRetry(db);
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch (Exception ex)
                {
                    Log($"Unexpected error: {ex.Message}", "ERROR");
                }
            }
        }, token);

        return new KeepAlive(client, cts, task);
    }

    /// <summary>
    /// Stop the keep-alive loop and release resources.
    /// </summary>
    public void Dispose()
    {
        _cts.Cancel();
        try { _task.Wait(TimeSpan.FromSeconds(5)); } catch { /* expected */ }
        _cts.Dispose();
        Log("Stopped and disconnected.");
    }

    private static void PingWithRetry(IMongoDatabase db)
    {
        for (var attempt = 1; attempt <= MaxRetries; attempt++)
        {
            try
            {
                var result = db.RunCommand<BsonDocument>(new BsonDocument("ping", 1));
                Log($"Ping successful: {result}");
                return;
            }
            catch (Exception ex)
            {
                Log($"Ping attempt {attempt}/{MaxRetries} failed: {ex.Message}", "WARN");
                if (attempt < MaxRetries)
                    Thread.Sleep(RetryDelay);
            }
        }
        Log($"All {MaxRetries} ping attempts failed. Will retry at next interval.", "ERROR");
    }

    private static TimeSpan ParseInterval(string interval)
    {
        var match = Regex.Match(interval.Trim(), @"^(\d+)\s*([hms])$", RegexOptions.IgnoreCase);
        if (!match.Success)
            throw new ArgumentException(
                $"Invalid interval \"{interval}\". Use e.g. \"12h\", \"30m\", or \"60s\".");

        var value = int.Parse(match.Groups[1].Value);
        var unit = match.Groups[2].Value.ToLowerInvariant();

        return unit switch
        {
            "h" => TimeSpan.FromHours(value),
            "m" => TimeSpan.FromMinutes(value),
            "s" => TimeSpan.FromSeconds(value),
            _ => throw new ArgumentException($"Unknown unit: {unit}")
        };
    }

    private static void Log(string message, string level = "INFO")
    {
        Console.WriteLine($"[{DateTime.UtcNow:O}] [{level}] [mongo-keepalive] {message}");
    }
}
