# mongo_keepalive

Keep MongoDB Atlas free-tier clusters alive by sending periodic `ping` commands.

## Installation

```bash
dart pub add mongo_keepalive
```

## Usage

```dart
import 'package:mongo_keepalive/keepalive.dart';

void main() async {
  final handle = await startKeepAlive(
    uri: 'mongodb+srv://user:pass@cluster.mongodb.net/db',
    interval: '12h', // optional, defaults to "12h"
  );

  // To stop later:
  // await handle.stop();
}
```

## API

### `startKeepAlive(uri, interval)`

| Parameter | Type | Default | Description |
| --------- | ---- | ------- | ------------------------------------ |
| `uri` | String | — | MongoDB connection string (required) |
| `interval` | String | `"12h"` | Ping interval (`"12h"`, `"30m"`, `"60s"`) |

Returns a `Future<KeepAliveHandle>`.

## License

MIT
