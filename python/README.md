# mongo-keepalive (Python)

Keep MongoDB Atlas free-tier clusters alive by sending periodic `ping` commands.

## Installation

```bash
pip install mongo-keepalive
```

## Usage

```python
from mongo_keepalive import start_keep_alive

handle = start_keep_alive(
    uri="mongodb+srv://user:pass@cluster.mongodb.net/db",
    interval="12h",  # optional, defaults to "12h"
)

# To stop later:
# handle.stop()
```

## API

### `start_keep_alive(uri, interval="12h")`

| Parameter  | Type | Default | Description                          |
| ---------- | ---- | ------- | ------------------------------------ |
| `uri`      | str  | —       | MongoDB connection string (required) |
| `interval` | str  | `"12h"` | Ping interval (`"12h"`, `"30m"`, `"60s"`) |

Returns a handle with a `stop()` method.

## License

MIT
