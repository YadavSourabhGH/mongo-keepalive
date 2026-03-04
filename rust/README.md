# mongo-keepalive (Rust)

[![Crates.io](https://img.shields.io/crates/v/mongo-keepalive?style=flat-square&logo=rust&label=crates.io)](https://crates.io/crates/mongo-keepalive)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

Add to your `Cargo.toml`:

```toml
[dependencies]
mongo-keepalive = "1.0.0"
tokio = { version = "1", features = ["rt-multi-thread", "macros"] }
```

## Usage

```rust
use mongo_keepalive::start_keep_alive;

#[tokio::main]
async fn main() {
    let handle = start_keep_alive(
        "mongodb+srv://user:pass@cluster.mongodb.net/db",
        "12h",
    ).await.unwrap();

    // Keep running...
    tokio::signal::ctrl_c().await.unwrap();
    
    // Graceful shutdown
    handle.stop().await;
}
```

## Features

- ⚡ **Async/await** - Built on Tokio for efficient concurrency
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- 📝 **Logging** - Uses the `log` crate for observability
- 🛡️ **Error handling** - Comprehensive error types
- ♻️ **Graceful shutdown** - Clean stop mechanism via handle

## How It Works

1. Connects to MongoDB using the official async driver
2. Spawns a background task that pings every interval
3. Retries failed pings automatically
4. Logs all operations for monitoring

## Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `uri` | `&str` | - | MongoDB connection string (required) |
| `interval` | `&str` | `"12h"` | Ping interval (e.g. `"6h"`, `"30m"`) |

### Interval Format

- `"30m"` → 30 minutes
- `"6h"` → 6 hours  
- `"12h"` → 12 hours (default)

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [Full Documentation](https://github.com/YadavSourabhGH/mongo-keepalive#readme)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
