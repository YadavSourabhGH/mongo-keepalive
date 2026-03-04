# mongo-keepalive (Node.js)

Keep MongoDB Atlas free-tier clusters alive by sending periodic `ping` commands.

## Installation

```bash
npm install mongo-keepalive
```

## Usage

```js
const { startKeepAlive } = require("mongo-keepalive");

(async () => {
  const handle = await startKeepAlive({
    uri: "mongodb+srv://user:pass@cluster.mongodb.net/db",
    interval: "12h", // optional, defaults to "12h"
  });

  // To stop later:
  // await handle.stop();
})();
```

## API

### `startKeepAlive(options)`

| Option     | Type   | Default | Description                          |
| ---------- | ------ | ------- | ------------------------------------ |
| `uri`      | string | —       | MongoDB connection string (required) |
| `interval` | string | `"12h"` | Ping interval (`"12h"`, `"30m"`, `"60s"`) |

Returns a `Promise<{ stop: () => Promise<void> }>`.

## License

MIT
