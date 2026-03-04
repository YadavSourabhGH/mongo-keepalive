# mongo-keepalive (Go)

[![Go Reference](https://pkg.go.dev/badge/github.com/YadavSourabhGH/mongo-keepalive/go.svg)](https://pkg.go.dev/github.com/YadavSourabhGH/mongo-keepalive/go)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

```bash
go get github.com/YadavSourabhGH/mongo-keepalive/go@v1.0.0
```

## Usage

```go
package main

import (
    "log"
    "os"
    "os/signal"
    "syscall"
    
    keepalive "github.com/YadavSourabhGH/mongo-keepalive/go"
)

func main() {
    // Start keep-alive with default 12-hour interval
    handle := keepalive.StartKeepAlive(keepalive.Options{
        URI: "mongodb+srv://user:pass@cluster.mongodb.net/db",
    })
    
    // Or specify custom interval
    handle := keepalive.StartKeepAlive(keepalive.Options{
        URI:      "mongodb+srv://user:pass@cluster.mongodb.net/db",
        Interval: "6h",
    })
    
    // Wait for interrupt signal
    sigChan := make(chan os.Signal, 1)
    signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
    <-sigChan
    
    // Graceful shutdown
    handle.Stop()
    log.Println("Shutdown complete")
}
```

## Features

- 🎯 **Idiomatic Go** - Context-based cancellation
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- 📝 **Logging** - Standard log package integration
- ⚡ **Efficient** - Uses time.Ticker for scheduling
- ♻️ **Graceful shutdown** - Context cancellation support
- 🔒 **Thread-safe** - Concurrent-safe implementation

## Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `URI` | string | - | MongoDB connection string (required) |
| `Interval` | string | `"12h"` | Ping interval (e.g. `"6h"`, `"30m"`) |

### Interval Format

- `"30m"` → 30 minutes
- `"6h"` → 6 hours  
- `"12h"` → 12 hours (default)

## Requirements

- Go 1.21 or later
- go.mongodb.org/mongo-driver v1.15.0+

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [pkg.go.dev Documentation](https://pkg.go.dev/github.com/YadavSourabhGH/mongo-keepalive/go)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
