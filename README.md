# mongo-keepalive

[![npm](https://img.shields.io/npm/v/mongo-keepalive?style=flat-square&logo=npm&label=npm)](https://www.npmjs.com/package/mongo-keepalive)
[![PyPI](https://img.shields.io/pypi/v/mongo-keepalive?style=flat-square&logo=pypi&label=PyPI)](https://pypi.org/project/mongo-keepalive/)
[![Crates.io](https://img.shields.io/crates/v/mongo-keepalive?style=flat-square&logo=rust&label=crates.io)](https://crates.io/crates/mongo-keepalive)
[![NuGet](https://img.shields.io/nuget/v/MongoKeepAlive?style=flat-square&logo=nuget&label=NuGet)](https://www.nuget.org/packages/MongoKeepAlive)
[![RubyGems](https://img.shields.io/gem/v/mongo-keepalive?style=flat-square&logo=rubygems&label=gem)](https://rubygems.org/gems/mongo-keepalive)
[![pub.dev](https://img.shields.io/pub/v/mongo_keepalive?style=flat-square&logo=dart&label=pub.dev)](https://pub.dev/packages/mongo_keepalive)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

---

## Supported Languages

| Language | Package | Status | Directory |
| -------- | ----------------------------- | ------ | ------------ |
| Node.js | [`mongo-keepalive`](https://www.npmjs.com/package/mongo-keepalive) | ✅ Published | `node/` |
| Python | [`mongo-keepalive`](https://pypi.org/project/mongo-keepalive/) | ✅ Published | `python/` |
| Go | `mongo-keepalive/go` | 📦 Tag-based | `go/` |
| Rust | [`mongo-keepalive`](https://crates.io/crates/mongo-keepalive) | ✅ Published | `rust/` |
| Java | `mongo-keepalive` | 🚧 Ready | `java/` |
| PHP | `mongo-keepalive` | 🚧 Ready | `php/` |
| Ruby | [`mongo-keepalive`](https://rubygems.org/gems/mongo-keepalive) | ✅ Published | `ruby/` |
| C# | [`MongoKeepAlive`](https://www.nuget.org/packages/MongoKeepAlive) | ✅ Published | `csharp/` |
| Dart | [`mongo_keepalive`](https://pub.dev/packages/mongo_keepalive) | ✅ Published | `dart/` |
| Kotlin | `mongo-keepalive` | 🚧 Ready | `kotlin/` |

**Status Legend:**
- ✅ Published: Available on package registry
- 📦 Tag-based: Install via GitHub tag/module path
- 🚧 Ready: Code complete, publishing in progress

---

## Why Use This?

- 🎯 **Simple**: One function call to keep your cluster alive
- 🔄 **Reliable**: Built-in retry logic handles transient failures
- ⚡ **Lightweight**: Minimal memory footprint and dependencies
- 🌍 **Multi-language**: Use the same solution across your entire stack
- 🔧 **Configurable**: Adjust ping intervals to your needs
- 📝 **Production-ready**: Comprehensive logging and error handling

---

## Installation

### Node.js

```bash
npm install mongo-keepalive
```

### Python

```bash
pip install mongo-keepalive
```

### Go

```bash
go get github.com/YadavSourabhGH/mongo-keepalive/go
```

### Rust

```toml
[dependencies]
mongo-keepalive = "1.0.0"
```

### Java / Kotlin (Maven)

```xml
<dependency>
  <groupId>com.mongokeepalive</groupId>
  <artifactId>mongo-keepalive</artifactId>
  <version>1.0.0</version>
</dependency>
```

### PHP

```bash
composer require YadavSourabhGH/mongo-keepalive
```

### Ruby

```bash
gem install mongo-keepalive
```

### C\#

```bash
dotnet add package MongoKeepAlive
```

### Dart

```bash
dart pub add mongo_keepalive
```

---

## Usage

Every implementation exposes a similar API:

```
startKeepAlive({
  uri: "mongodb+srv://user:pass@cluster.mongodb.net/db",
  interval: "12h"
})
```

### Node.js

```js
const { startKeepAlive } = require("mongo-keepalive");

startKeepAlive({
  uri: "mongodb+srv://user:pass@cluster.mongodb.net/db",
  interval: "12h",
});
```

### Python

```python
from mongo_keepalive import start_keep_alive

start_keep_alive(
    uri="mongodb+srv://user:pass@cluster.mongodb.net/db",
    interval="12h",
)
```

### Go

```go
package main

import keepalive "github.com/YadavSourabhGH/mongo-keepalive/go"

func main() {
    keepalive.StartKeepAlive(keepalive.Options{
        URI:      "mongodb+srv://user:pass@cluster.mongodb.net/db",
        Interval: "12h",
    })
}
```

### Rust

```rust
use mongo_keepalive::start_keep_alive;

#[tokio::main]
async fn main() {
    start_keep_alive(
        "mongodb+srv://user:pass@cluster.mongodb.net/db",
        "12h",
    ).await;
}
```

### Java

```java
import com.mongokeepalive.KeepAlive;

public class Main {
    public static void main(String[] args) {
        KeepAlive.startKeepAlive(
            "mongodb+srv://user:pass@cluster.mongodb.net/db",
            "12h"
        );
    }
}
```

### PHP

```php
use MongoKeepAlive\KeepAlive;

KeepAlive::startKeepAlive(
    'mongodb+srv://user:pass@cluster.mongodb.net/db',
    '12h'
);
```

### Ruby

```ruby
require "mongo_keepalive"

MongoKeepAlive.start_keep_alive(
  uri: "mongodb+srv://user:pass@cluster.mongodb.net/db",
  interval: "12h"
)
```

### C\#

```csharp
using MongoKeepAlive;

KeepAlive.StartKeepAlive(
    "mongodb+srv://user:pass@cluster.mongodb.net/db",
    "12h"
);
```

### Dart

```dart
import 'package:mongo_keepalive/keepalive.dart';

void main() {
  startKeepAlive(
    uri: 'mongodb+srv://user:pass@cluster.mongodb.net/db',
    interval: '12h',
  );
}
```

### Kotlin

```kotlin
import com.mongokeepalive.KeepAlive

fun main() {
    KeepAlive.startKeepAlive(
        uri = "mongodb+srv://user:pass@cluster.mongodb.net/db",
        interval = "12h"
    )
}
```

---

## How It Works

1. **Connects** to your MongoDB cluster using the official driver for each language.
2. **Pings** the database with `db.adminCommand({ ping: 1 })` at the configured interval.
3. **Retries** automatically on transient failures (3 attempts with 5-second delays).
4. **Logs** every ping attempt and result with timestamps.
5. **Runs efficiently** — uses a single lightweight timer/scheduler with minimal memory usage.

The ping command is a lightweight operation that doesn't impact your cluster's performance but successfully registers as "activity" to prevent automatic pausing.

---

## Configuration

| Option | Type | Default | Description |
| ---------- | ------ | ------- | ------------------------------------ |
| `uri` | string | — | MongoDB connection string (required) |
| `interval` | string | `"12h"` | Ping interval (e.g. `"6h"`, `"30m"`) |

### Interval Format

- `"30m"` → 30 minutes
- `"6h"` → 6 hours
- `"12h"` → 12 hours (default)

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feat/my-feature`.
3. Commit your changes: `git commit -m "feat: add my feature"`.
4. Push to the branch: `git push origin feat/my-feature`.
5. Open a Pull Request.

Please ensure your code:

- Follows the idiomatic conventions of the target language.
- Includes logging and retry logic.
- Does not introduce unnecessary dependencies.

---

## Features

✨ **Cross-platform compatibility**: Works across all major programming languages and ecosystems  
🔐 **Secure**: Uses official MongoDB drivers with standard connection strings  
⏱️ **Flexible intervals**: Configure from minutes to hours based on your needs  
🛡️ **Error resilient**: Automatic retry logic with exponential backoff  
📊 **Observable**: Built-in logging for monitoring and debugging  
🚀 **Zero-config defaults**: Works out of the box with sensible 12-hour interval  
♻️ **Graceful shutdown**: Clean stop mechanisms in all implementations  
🎨 **Idiomatic code**: Each language implementation follows best practices and conventions  

---

## FAQ

**Q: Will this increase my MongoDB Atlas costs?**  
A: No. The ping command is extremely lightweight and doesn't count against your storage or compute quotas. Free-tier clusters remain free.

**Q: How often should I ping?**  
A: The default 12-hour interval is recommended. This is frequent enough to prevent inactivity while minimizing unnecessary operations.

**Q: Can I use this in production?**  
A: Yes! All implementations include proper error handling, logging, and retry logic suitable for production use.

**Q: Does this work with paid MongoDB Atlas tiers?**  
A: Yes, but it's primarily designed for free-tier (M0) clusters which auto-pause after 60 days of inactivity.

**Q: What happens if my internet connection drops?**  
A: The library will retry failed pings automatically and continue when connectivity is restored.

---

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📝 [Publishing Guide](PUBLISHING.md)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)
- 💡 [Request Features](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

---

## License

This project is licensed under the [MIT License](LICENSE).
