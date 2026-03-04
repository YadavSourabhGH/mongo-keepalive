# mongo-keepalive (Java)

[![Maven Central](https://img.shields.io/maven-central/v/com.mongokeepalive/mongo-keepalive?style=flat-square&logo=apachemaven&label=Maven%20Central)](https://central.sonatype.com/artifact/com.mongokeepalive/mongo-keepalive)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>com.mongokeepalive</groupId>
  <artifactId>mongo-keepalive</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```gradle
implementation 'com.mongokeepalive:mongo-keepalive:1.0.0'
```

## Usage

```java
import com.mongokeepalive.KeepAlive;

public class Main {
    public static void main(String[] args) {
        // Start keep-alive with default 12-hour interval
        var handle = KeepAlive.startKeepAlive(
            "mongodb+srv://user:pass@cluster.mongodb.net/db"
        );
        
        // Or specify custom interval
        var handle = KeepAlive.startKeepAlive(
            "mongodb+srv://user:pass@cluster.mongodb.net/db",
            "6h"
        );
        
        // Keep running...
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            handle.stop();
        }));
    }
}
```

## Features

- 🎯 **Simple API** - Static utility class
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- 📝 **Logging** - SLF4J integration for flexible logging
- ⚡ **Efficient scheduling** - ScheduledExecutorService with daemon threads
- ♻️ **Graceful shutdown** - Handle-based stop mechanism

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

- Java 11 or later
- MongoDB Java Driver 5.1.0+
- SLF4J 2.0.13+

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [Full Documentation](https://github.com/YadavSourabhGH/mongo-keepalive#readme)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
