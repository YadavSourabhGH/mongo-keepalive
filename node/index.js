"use strict";

const { MongoClient } = require("mongodb");

const DEFAULT_INTERVAL = "12h";
const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 5000;

/**
 * Parse an interval string (e.g. "12h", "30m") into milliseconds.
 * @param {string} interval
 * @returns {number}
 */
function parseInterval(interval) {
  const match = interval.trim().match(/^(\d+)\s*(h|m|s)$/i);
  if (!match) {
    throw new Error(
      `Invalid interval format "${interval}". Use "12h", "30m", or "60s".`
    );
  }
  const value = parseInt(match[1], 10);
  const unit = match[2].toLowerCase();
  const multipliers = { h: 3_600_000, m: 60_000, s: 1_000 };
  return value * multipliers[unit];
}

/**
 * Sleep for the given number of milliseconds.
 * @param {number} ms
 * @returns {Promise<void>}
 */
function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Send a single ping command, retrying up to MAX_RETRIES times on failure.
 * @param {import("mongodb").Db} db
 */
async function pingWithRetry(db) {
  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    try {
      const result = await db.admin().command({ ping: 1 });
      console.log(
        `[mongo-keepalive] Ping successful at ${new Date().toISOString()}`,
        result
      );
      return;
    } catch (err) {
      console.error(
        `[mongo-keepalive] Ping attempt ${attempt}/${MAX_RETRIES} failed:`,
        err.message
      );
      if (attempt < MAX_RETRIES) {
        await sleep(RETRY_DELAY_MS);
      }
    }
  }
  console.error(
    `[mongo-keepalive] All ${MAX_RETRIES} ping attempts failed. Will retry at next interval.`
  );
}

/**
 * Start the keep-alive loop.
 *
 * @param {object} options
 * @param {string} options.uri    - MongoDB connection string.
 * @param {string} [options.interval="12h"] - Ping interval (e.g. "12h", "30m").
 * @returns {Promise<{ stop: () => Promise<void> }>} Handle with a stop() method.
 */
async function startKeepAlive({ uri, interval = DEFAULT_INTERVAL } = {}) {
  if (!uri) {
    throw new Error("[mongo-keepalive] A MongoDB connection URI is required.");
  }

  const intervalMs = parseInterval(interval);
  console.log(
    `[mongo-keepalive] Starting with interval ${interval} (${intervalMs} ms)`
  );

  const client = new MongoClient(uri);
  await client.connect();
  console.log("[mongo-keepalive] Connected to MongoDB.");

  const db = client.db("admin");

  // Initial ping
  await pingWithRetry(db);

  // Schedule periodic pings
  const timer = setInterval(() => {
    pingWithRetry(db).catch((err) => {
      console.error("[mongo-keepalive] Unexpected error during ping:", err);
    });
  }, intervalMs);

  // Prevent the timer from keeping the process alive if nothing else does
  if (timer.unref) {
    timer.unref();
  }

  return {
    /**
     * Stop the keep-alive loop and close the MongoDB connection.
     */
    async stop() {
      clearInterval(timer);
      await client.close();
      console.log("[mongo-keepalive] Stopped and disconnected.");
    },
  };
}

module.exports = { startKeepAlive };
