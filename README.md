# mongo-keepalive

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

---

## Supported Languages

| Language | Package | Directory |
| -------- | ----------------------------- | ------------ |
| Node.js | `mongo-keepalive` | `node/` |
| Python | `mongo-keepalive` | `python/` |
| Go | `mongo-keepalive/go` | `go/` |
| Rust | `mongo-keepalive` | `rust/` |
| Java | `mongo-keepalive` | `java/` |
| PHP | `mongo-keepalive` | `php/` |
| Ruby | `mongo-keepalive` | `ruby/` |
| C# | `MongoKeepAlive` | `csharp/` |
| Dart | `mongo_keepalive` | `dart/` |
| Kotlin | `mongo-keepalive` | `kotlin/` |

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

1. Connects to your MongoDB cluster using the official driver.
2. Sends `db.adminCommand({ ping: 1 })` at the configured interval.
3. Retries automatically on transient failures.
4. Logs every ping attempt and result.
5. Uses minimal memory — a single lightweight timer/scheduler.

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

## License

This project is licensed under the [MIT License](LICENSE).
