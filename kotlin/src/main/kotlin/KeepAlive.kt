package com.mongokeepalive

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Keep MongoDB Atlas free-tier clusters alive by periodically sending a ping command.
 */
object KeepAlive {

    private val logger = LoggerFactory.getLogger(KeepAlive::class.java)
    private const val DEFAULT_INTERVAL = "12h"
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 5000L
    private val INTERVAL_REGEX = Regex("""(?i)(\d+)\s*([hms])""")

    /**
     * A handle to the keep-alive scheduler. Call [stop] to cancel.
     */
    class Handle internal constructor(
        private val client: com.mongodb.client.MongoClient,
        private val scheduler: ScheduledExecutorService,
    ) {
        /** Stop the keep-alive loop and close the connection. */
        fun stop() {
            scheduler.shutdownNow()
            client.close()
            logger.info("[mongo-keepalive] Stopped and disconnected.")
        }
    }

    /**
     * Start the keep-alive loop.
     *
     * @param uri      MongoDB connection string.
     * @param interval Ping interval, e.g. "12h", "30m", "60s". Defaults to "12h".
     * @return A [Handle] that can be used to [Handle.stop] the loop.
     */
    @JvmStatic
    @JvmOverloads
    fun startKeepAlive(uri: String, interval: String = DEFAULT_INTERVAL): Handle {
        require(uri.isNotBlank()) { "[mongo-keepalive] A MongoDB connection URI is required." }

        val effectiveInterval = interval.ifBlank { DEFAULT_INTERVAL }
        val intervalMs = parseInterval(effectiveInterval)
        logger.info("[mongo-keepalive] Starting with interval {} ({} ms)", effectiveInterval, intervalMs)

        val client = MongoClients.create(uri)
        val db: MongoDatabase = client.getDatabase("admin")

        // Initial ping
        pingWithRetry(db)

        val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "mongo-keepalive").apply { isDaemon = true }
        }

        scheduler.scheduleAtFixedRate({
            try {
                pingWithRetry(db)
            } catch (e: Exception) {
                logger.error("[mongo-keepalive] Unexpected error during ping", e)
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS)

        return Handle(client, scheduler)
    }

    private fun pingWithRetry(db: MongoDatabase) {
        for (attempt in 1..MAX_RETRIES) {
            try {
                val result: Document = db.runCommand(Document("ping", 1))
                logger.info("[mongo-keepalive] Ping successful: {}", result.toJson())
                return
            } catch (e: Exception) {
                logger.warn("[mongo-keepalive] Ping attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.message)
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS)
                }
            }
        }
        logger.error("[mongo-keepalive] All {} ping attempts failed. Will retry at next interval.", MAX_RETRIES)
    }

    private fun parseInterval(interval: String): Long {
        val match = INTERVAL_REGEX.matchEntire(interval.trim())
            ?: throw IllegalArgumentException(
                """Invalid interval "$interval". Use e.g. "12h", "30m", or "60s"."""
            )
        val value = match.groupValues[1].toLong()
        return when (match.groupValues[2].lowercase()) {
            "h" -> value * 3_600_000
            "m" -> value * 60_000
            "s" -> value * 1_000
            else -> throw IllegalArgumentException("Unknown unit in interval: $interval")
        }
    }
}
