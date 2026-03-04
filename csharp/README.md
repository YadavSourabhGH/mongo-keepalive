# MongoKeepAlive (C#/.NET)

[![NuGet](https://img.shields.io/nuget/v/MongoKeepAlive?style=flat-square&logo=nuget&label=NuGet)](https://www.nuget.org/packages/MongoKeepAlive)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

```bash
dotnet add package MongoKeepAlive
```

## Usage

```csharp
using MongoKeepAlive;

// Start keep-alive with default 12-hour intervalvar keepAlive = KeepAlive.StartKeepAlive(
    "mongodb+srv://user:pass@cluster.mongodb.net/db"
);

// Or specify custom interval
var keepAlive = KeepAlive.StartKeepAlive(
    "mongodb+srv://user:pass@cluster.mongodb.net/db",
    "6h"
);

// Graceful shutdown
keepAlive.Dispose();
```

## Features

- 🎯 **Simple API** - One method call to start
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- ⚡ **Async/await** - Built on Task-based async
- 📝 **Logging** - Console output with timestamps
- 🛡️ **IDisposable** - Proper resource cleanup
- ♻️ **Graceful shutdown** - Cancellation token support

## Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `uri` | `string` | - | MongoDB connection string (required) |
| `interval` | `string` | `"12h"` | Ping interval (e.g. `"6h"`, `"30m"`) |

### Interval Format

- `"30m"` → 30 minutes
- `"6h"` → 6 hours  
- `"12h"` → 12 hours (default)

## Requirements

- .NET 8.0 or later
- MongoDB.Driver 2.28.0+

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [Full Documentation](https://github.com/YadavSourabhGH/mongo-keepalive#readme)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
