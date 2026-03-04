# mongo-keepalive (PHP)

[![Packagist Version](https://img.shields.io/packagist/v/yadavsourabhgh/mongo-keepalive?style=flat-square&logo=packagist&label=packagist)](https://packagist.org/packages/yadavsourabhgh/mongo-keepalive)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

Install via Composer:

```bash
composer require yadavsourabhgh/mongo-keepalive
```

## Usage

```php
<?php
require 'vendor/autoload.php';

use MongoKeepAlive\KeepAlive;

// Start keep-alive with default 12-hour interval
KeepAlive::startKeepAlive(
    'mongodb+srv://user:pass@cluster.mongodb.net/db'
);

// Or specify custom interval
KeepAlive::startKeepAlive(
    'mongodb+srv://user:pass@cluster.mongodb.net/db',
    '6h'
);

// The function runs in a blocking loop. Use Ctrl+C to stop.
```

## Features

- 🎯 **Simple API** - Static method for easy usage
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- 📝 **Logging** - Echoes timestamped messages
- ⚡ **Signal handling** - Responds to SIGINT/SIGTERM
- ♻️ **Graceful shutdown** - Clean exit on signals

## Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `$uri` | string | - | MongoDB connection string (required) |
| `$interval` | string | `"12h"` | Ping interval (e.g. `"6h"`, `"30m"`) |

### Interval Format

- `"30m"` → 30 minutes
- `"6h"` → 6 hours  
- `"12h"` → 12 hours (default)

## Requirements

- PHP >= 8.1
- mongodb/mongodb ^1.19
- ext-mongodb

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [Full Documentation](https://github.com/YadavSourabhGH/mongo-keepalive#readme)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
