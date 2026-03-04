//! # mongo-keepalive
//!
//! Keep MongoDB Atlas free-tier clusters alive by periodically sending a `ping` command.

use log::{error, info, warn};
use mongodb::{bson::doc, Client};
use std::time::Duration;
use tokio::time;

const DEFAULT_INTERVAL: &str = "12h";
const MAX_RETRIES: u32 = 3;
const RETRY_DELAY: Duration = Duration::from_secs(5);

/// Parse an interval string like `"12h"`, `"30m"`, or `"60s"` into a [`Duration`].
///
/// # Errors
/// Returns an error if the format is invalid.
pub fn parse_interval(interval: &str) -> Result<Duration, String> {
    let s = interval.trim();
    if s.is_empty() {
        return Err("Interval must not be empty".into());
    }

    let (digits, unit) = s.split_at(s.len() - 1);
    let value: u64 = digits
        .parse()
        .map_err(|_| format!("Invalid interval \"{interval}\". Use e.g. \"12h\", \"30m\", or \"60s\"."))?;

    match unit.to_ascii_lowercase().as_str() {
        "h" => Ok(Duration::from_secs(value * 3600)),
        "m" => Ok(Duration::from_secs(value * 60)),
        "s" => Ok(Duration::from_secs(value)),
        _ => Err(format!(
            "Invalid interval \"{interval}\". Use e.g. \"12h\", \"30m\", or \"60s\"."
        )),
    }
}

/// Send a ping command with retry logic.
async fn ping_with_retry(client: &Client) {
    let db = client.database("admin");
    for attempt in 1..=MAX_RETRIES {
        match db.run_command(doc! { "ping": 1 }, None).await {
            Ok(result) => {
                info!("[mongo-keepalive] Ping successful: {:?}", result);
                return;
            }
            Err(err) => {
                warn!(
                    "[mongo-keepalive] Ping attempt {}/{} failed: {}",
                    attempt, MAX_RETRIES, err
                );
                if attempt < MAX_RETRIES {
                    time::sleep(RETRY_DELAY).await;
                }
            }
        }
    }
    error!(
        "[mongo-keepalive] All {} ping attempts failed. Will retry at next interval.",
        MAX_RETRIES
    );
}

/// A handle to the keep-alive background task. Dropping it cancels the task.
pub struct KeepAliveHandle {
    handle: tokio::task::JoinHandle<()>,
}

impl KeepAliveHandle {
    /// Stop the keep-alive loop.
    pub fn stop(self) {
        self.handle.abort();
        info!("[mongo-keepalive] Stopped.");
    }
}

/// Start the keep-alive loop.
///
/// Connects to MongoDB using `uri`, sends an initial ping, then repeats at the
/// given `interval` (default `"12h"`).
///
/// Returns a [`KeepAliveHandle`] that can be used to stop the loop.
///
/// # Panics
/// Panics if the MongoDB connection cannot be established.
pub async fn start_keep_alive(uri: &str, interval: &str) -> KeepAliveHandle {
    let interval = if interval.is_empty() {
        DEFAULT_INTERVAL
    } else {
        interval
    };

    let dur = parse_interval(interval).expect("Invalid interval");
    info!(
        "[mongo-keepalive] Starting with interval {} ({:?})",
        interval, dur
    );

    let client = Client::with_uri_str(uri)
        .await
        .expect("[mongo-keepalive] Failed to connect to MongoDB");

    // Initial ping
    ping_with_retry(&client).await;

    let handle = tokio::spawn(async move {
        let mut ticker = time::interval(dur);
        // Skip the first tick (already pinged above)
        ticker.tick().await;

        loop {
            ticker.tick().await;
            ping_with_retry(&client).await;
        }
    });

    KeepAliveHandle { handle }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_parse_interval() {
        assert_eq!(parse_interval("12h").unwrap(), Duration::from_secs(43200));
        assert_eq!(parse_interval("30m").unwrap(), Duration::from_secs(1800));
        assert_eq!(parse_interval("60s").unwrap(), Duration::from_secs(60));
        assert!(parse_interval("abc").is_err());
        assert!(parse_interval("").is_err());
    }
}
