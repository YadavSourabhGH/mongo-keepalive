# mongo-keepalive (Kotlin)

[![Maven Central](https://img.shields.io/maven-central/v/com.mongokeepalive/mongo-keepalive-kotlin?style=flat-square&logo=apachemaven&label=Maven%20Central)](https://central.sonatype.com/artifact/com.mongokeepalive/mongo-keepalive-kotlin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>com.mongokeepalive</groupId>
  <artifactId>mongo-keepalive-kotlin</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle Kotlin DSL

Add to your `build.gradle.kts`:

```kotlin
implementation("com.mongokeepalive:mongo-keepalive-kotlin:1.0.0")
```

## Usage

```kotlin
import com.mongokeepalive.KeepAlive

fun main() {
    // Start keep-alive with default 12-hour interval
    val handle = KeepAlive.startKeepAlive(
        uri = "mongodb+srv://user:pass@cluster.mongodb.net/db"
    )
    
    // Or specify custom interval
    val handle = KeepAlive.startKeepAlive(
        uri = "mongodb+srv://user:pass@cluster.mongodb.net/db",
        interval = "6h"
    )
    
    // Keep running...
    Runtime.getRuntime().addShutdownHook(Thread {
        handle.stop()
    })
}
```

## Features

- 🎯 **Idiomatic Kotlin** - Named parameters and concise syntax
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- 📝 **Logging** - SLF4J integration for flexible logging
- ⚡ **Efficient scheduling** - ScheduledExecutorService with daemon threads
- ♻️ **Graceful shutdown** - Handle-based stop mechanism
- ☕ **Java interop** - Works seamlessly with Java code

## Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `uri` | String | - | MongoDB connection string (required) |
| `interval` | String | `"12h"` | Ping interval (e.g. `"6h"`, `"30m"`) |

### Interval Format

- `"30m"` → 30 minutes
- `"6h"` → 6 hours  
- `"12h"` → 12 hours (default)

## Requirements

- Kotlin 1.9.24 or later
- Java 11 or later
- MongoDB Java Driver 5.1.0+
- SLF4J 2.0.13+

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [Full Documentation](https://github.com/YadavSourabhGH/mongo-keepalive#readme)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
