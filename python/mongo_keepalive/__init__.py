"""mongo-keepalive: Keep MongoDB Atlas free-tier clusters alive."""

from __future__ import annotations

import logging
import re
import threading
import time
from typing import Optional

from pymongo import MongoClient
from pymongo.errors import PyMongoError

__all__ = ["start_keep_alive"]
__version__ = "1.0.0"

logger = logging.getLogger("mongo_keepalive")

_DEFAULT_INTERVAL = "12h"
_MAX_RETRIES = 3
_RETRY_DELAY_S = 5


def _parse_interval(interval: str) -> float:
    """Parse an interval string like '12h', '30m', or '60s' into seconds."""
    match = re.fullmatch(r"(\d+)\s*([hms])", interval.strip(), re.IGNORECASE)
    if not match:
        raise ValueError(
            f'Invalid interval "{interval}". Use e.g. "12h", "30m", or "60s".'
        )
    value = int(match.group(1))
    unit = match.group(2).lower()
    multipliers = {"h": 3600, "m": 60, "s": 1}
    return float(value * multipliers[unit])


def _ping_with_retry(client: MongoClient) -> None:
    """Send a ping command, retrying up to _MAX_RETRIES times on failure."""
    for attempt in range(1, _MAX_RETRIES + 1):
        try:
            result = client.admin.command("ping")
            logger.info("Ping successful: %s", result)
            return
        except PyMongoError as exc:
            logger.warning(
                "Ping attempt %d/%d failed: %s", attempt, _MAX_RETRIES, exc
            )
            if attempt < _MAX_RETRIES:
                time.sleep(_RETRY_DELAY_S)
    logger.error(
        "All %d ping attempts failed. Will retry at next interval.", _MAX_RETRIES
    )


class _KeepAliveHandle:
    """Control handle returned by start_keep_alive."""

    def __init__(self, client: MongoClient, stop_event: threading.Event) -> None:
        self._client = client
        self._stop_event = stop_event

    def stop(self) -> None:
        """Stop the keep-alive loop and close the connection."""
        self._stop_event.set()
        self._client.close()
        logger.info("Stopped and disconnected.")


def start_keep_alive(
    uri: str,
    interval: str = _DEFAULT_INTERVAL,
) -> _KeepAliveHandle:
    """Start the keep-alive loop in a background daemon thread.

    Args:
        uri: MongoDB connection string.
        interval: Ping interval, e.g. ``"12h"``, ``"30m"``, ``"60s"``.

    Returns:
        A handle with a ``stop()`` method.
    """
    if not uri:
        raise ValueError("A MongoDB connection URI is required.")

    interval_s = _parse_interval(interval)
    logger.info("Starting with interval %s (%.0f s)", interval, interval_s)

    client = MongoClient(uri)

    # Verify connectivity with an initial ping
    _ping_with_retry(client)

    stop_event = threading.Event()

    def _loop() -> None:
        while not stop_event.wait(timeout=interval_s):
            _ping_with_retry(client)

    thread = threading.Thread(target=_loop, daemon=True, name="mongo-keepalive")
    thread.start()

    return _KeepAliveHandle(client, stop_event)
