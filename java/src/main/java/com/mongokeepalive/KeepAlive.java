package com.mongokeepalive;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keep MongoDB Atlas free-tier clusters alive by periodically sending a ping command.
 */
public final class KeepAlive {

    private static final Logger logger = LoggerFactory.getLogger(KeepAlive.class);
    private static final String DEFAULT_INTERVAL = "12h";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("(?i)(\\d+)\\s*([hms])");

    private final MongoClient client;
    private final ScheduledExecutorService scheduler;

    private KeepAlive(MongoClient client, ScheduledExecutorService scheduler) {
        this.client = client;
        this.scheduler = scheduler;
    }

    /**
     * Start the keep-alive loop with default interval (12h).
     *
     * @param uri MongoDB connection string
     * @return a KeepAlive handle that can be stopped
     */
    public static KeepAlive startKeepAlive(String uri) {
        return startKeepAlive(uri, DEFAULT_INTERVAL);
    }

    /**
     * Start the keep-alive loop.
     *
     * @param uri      MongoDB connection string
     * @param interval ping interval, e.g. "12h", "30m", "60s"
     * @return a KeepAlive handle that can be stopped
     */
    public static KeepAlive startKeepAlive(String uri, String interval) {
        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("[mongo-keepalive] A MongoDB connection URI is required.");
        }
        if (interval == null || interval.isBlank()) {
            interval = DEFAULT_INTERVAL;
        }

        long intervalMs = parseInterval(interval);
        logger.info("[mongo-keepalive] Starting with interval {} ({} ms)", interval, intervalMs);

        MongoClient client = MongoClients.create(uri);
        MongoDatabase db = client.getDatabase("admin");

        // Initial ping
        pingWithRetry(db);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mongo-keepalive");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                pingWithRetry(db);
            } catch (Exception e) {
                logger.error("[mongo-keepalive] Unexpected error during ping", e);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);

        return new KeepAlive(client, scheduler);
    }

    /**
     * Stop the keep-alive loop and close the MongoDB connection.
     */
    public void stop() {
        scheduler.shutdownNow();
        client.close();
        logger.info("[mongo-keepalive] Stopped and disconnected.");
    }

    private static void pingWithRetry(MongoDatabase db) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Document result = db.runCommand(new Document("ping", 1));
                logger.info("[mongo-keepalive] Ping successful: {}", result.toJson());
                return;
            } catch (Exception e) {
                logger.warn("[mongo-keepalive] Ping attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        logger.error("[mongo-keepalive] All {} ping attempts failed. Will retry at next interval.", MAX_RETRIES);
    }

    private static long parseInterval(String interval) {
        Matcher matcher = INTERVAL_PATTERN.matcher(interval.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Invalid interval \"%s\". Use e.g. \"12h\", \"30m\", or \"60s\".", interval));
        }
        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();
        return switch (unit) {
            case "h" -> value * 3_600_000;
            case "m" -> value * 60_000;
            case "s" -> value * 1_000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
}
