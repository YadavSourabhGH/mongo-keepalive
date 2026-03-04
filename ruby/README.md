# mongo-keepalive (Ruby)

[![Gem Version](https://img.shields.io/gem/v/mongo-keepalive?style=flat-square&logo=rubygems&label=gem)](https://rubygems.org/gems/mongo-keepalive)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](../LICENSE)

Prevent MongoDB Atlas free-tier clusters from becoming inactive by periodically sending a `ping` command.

MongoDB Atlas pauses free-tier (M0) clusters after **60 days of inactivity**. This library keeps your cluster alive by running `db.adminCommand({ ping: 1 })` on a configurable interval (default: every 12 hours).

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'mongo-keepalive'
```

And then execute:

```bash
bundle install
```

Or install it yourself as:

```bash
gem install mongo-keepalive
```

## Usage

```ruby
require 'mongo_keepalive'

# Start keep-alive with default 12-hour interval
handle = MongoKeepAlive.start_keep_alive(
  uri: 'mongodb+srv://user:pass@cluster.mongodb.net/db'
)

# Or specify custom interval
handle = MongoKeepAlive.start_keep_alive(
  uri: 'mongodb+srv://user:pass@cluster.mongodb.net/db',
  interval: '6h'
)

# Graceful shutdown
handle.stop
```

## Features

- 🎯 **Simple API** - One method call to start
- 🔄 **Automatic retry** - 3 attempts with 5-second delays
- 📝 **Logging** - Built-in logger with timestamps
- 🧵 **Background thread** - Non-blocking execution
- ♻️ **Graceful shutdown** - Clean stop mechanism

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

- Ruby 3.0 or later
- mongo gem ~> 2.20

## Links

- 📦 [GitHub Repository](https://github.com/YadavSourabhGH/mongo-keepalive)
- 📚 [Full Documentation](https://github.com/YadavSourabhGH/mongo-keepalive#readme)
- 🐛 [Report Issues](https://github.com/YadavSourabhGH/mongo-keepalive/issues)

## License

This project is licensed under the [MIT License](../LICENSE).
