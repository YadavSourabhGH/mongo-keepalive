/// Keep MongoDB Atlas free-tier clusters alive by periodically sending a ping
/// command.
library mongo_keepalive;

import 'dart:async';

import 'package:mongo_dart/mongo_dart.dart';

const String _defaultInterval = '12h';
const int _maxRetries = 3;
const Duration _retryDelay = Duration(seconds: 5);

/// Parse an interval string like `"12h"`, `"30m"`, or `"60s"` into a
/// [Duration].
Duration parseInterval(String interval) {
  final match = RegExp(r'^(\d+)\s*([hms])$', caseSensitive: false)
      .firstMatch(interval.trim());
  if (match == null) {
    throw ArgumentError(
        'Invalid interval "$interval". Use e.g. "12h", "30m", or "60s".');
  }
  final value = int.parse(match.group(1)!);
  final unit = match.group(2)!.toLowerCase();
  switch (unit) {
    case 'h':
      return Duration(hours: value);
    case 'm':
      return Duration(minutes: value);
    case 's':
      return Duration(seconds: value);
    default:
      throw ArgumentError('Unknown unit: $unit');
  }
}

void _log(String message, [String level = 'INFO']) {
  final now = DateTime.now().toUtc().toIso8601String();
  print('[$now] [$level] [mongo-keepalive] $message');
}

Future<void> _pingWithRetry(Db db) async {
  for (var attempt = 1; attempt <= _maxRetries; attempt++) {
    try {
      final result = await db.command({'ping': 1});
      _log('Ping successful: $result');
      return;
    } catch (e) {
      _log('Ping attempt $attempt/$_maxRetries failed: $e', 'WARN');
      if (attempt < _maxRetries) {
        await Future.delayed(_retryDelay);
      }
    }
  }
  _log('All $_maxRetries ping attempts failed. Will retry at next interval.',
      'ERROR');
}

/// A handle to the keep-alive timer. Call [stop] to cancel.
class KeepAliveHandle {
  final Db _db;
  final Timer _timer;

  KeepAliveHandle._(this._db, this._timer);

  /// Stop the keep-alive loop and close the connection.
  Future<void> stop() async {
    _timer.cancel();
    await _db.close();
    _log('Stopped and disconnected.');
  }
}

/// Start the keep-alive loop.
///
/// Connects to MongoDB using [uri], sends an initial ping, then repeats at the
/// given [interval] (default `"12h"`).
///
/// Returns a [KeepAliveHandle] that can be used to stop the loop.
Future<KeepAliveHandle> startKeepAlive({
  required String uri,
  String interval = _defaultInterval,
}) async {
  if (uri.isEmpty) {
    throw ArgumentError('A MongoDB connection URI is required.');
  }

  final duration = parseInterval(interval);
  _log('Starting with interval $interval (${duration.inMilliseconds} ms)');

  final db = Db(uri);
  await db.open();
  _log('Connected to MongoDB.');

  // Initial ping
  await _pingWithRetry(db);

  // Schedule periodic pings
  final timer = Timer.periodic(duration, (_) {
    _pingWithRetry(db).catchError((e) {
      _log('Unexpected error during ping: $e', 'ERROR');
    });
  });

  return KeepAliveHandle._(db, timer);
}
