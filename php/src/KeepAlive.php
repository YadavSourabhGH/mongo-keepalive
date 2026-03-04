<?php

declare(strict_types=1);

namespace MongoKeepAlive;

use MongoDB\Client;
use MongoDB\Driver\Exception\Exception as MongoException;

/**
 * Keep MongoDB Atlas free-tier clusters alive by periodically sending a ping command.
 */
final class KeepAlive
{
    private const DEFAULT_INTERVAL = '12h';
    private const MAX_RETRIES = 3;
    private const RETRY_DELAY_S = 5;

    private Client $client;
    private bool $running = false;

    private function __construct(Client $client)
    {
        $this->client = $client;
    }

    /**
     * Start the keep-alive loop (blocking).
     *
     * This method blocks indefinitely, pinging MongoDB at the configured interval.
     * Terminate the process or send SIGINT to stop.
     *
     * @param string $uri      MongoDB connection string.
     * @param string $interval Ping interval, e.g. "12h", "30m", "60s".
     */
    public static function startKeepAlive(string $uri, string $interval = self::DEFAULT_INTERVAL): void
    {
        if (empty($uri)) {
            throw new \InvalidArgumentException('[mongo-keepalive] A MongoDB connection URI is required.');
        }

        $intervalS = self::parseInterval($interval);
        self::log("Starting with interval {$interval} ({$intervalS} s)");

        $client = new Client($uri);
        $instance = new self($client);
        $instance->running = true;

        // Handle SIGINT/SIGTERM for graceful shutdown
        if (function_exists('pcntl_signal')) {
            pcntl_signal(SIGINT, function () use ($instance) {
                $instance->stop();
            });
            pcntl_signal(SIGTERM, function () use ($instance) {
                $instance->stop();
            });
        }

        // Initial ping
        $instance->pingWithRetry();

        // Periodic loop
        while ($instance->running) {
            sleep($intervalS);
            if (!$instance->running) {
                break;
            }
            $instance->pingWithRetry();

            if (function_exists('pcntl_signal_dispatch')) {
                pcntl_signal_dispatch();
            }
        }
    }

    /**
     * Stop the keep-alive loop.
     */
    public function stop(): void
    {
        $this->running = false;
        self::log('Stopped and disconnected.');
    }

    private function pingWithRetry(): void
    {
        $db = $this->client->selectDatabase('admin');

        for ($attempt = 1; $attempt <= self::MAX_RETRIES; $attempt++) {
            try {
                $result = $db->command(['ping' => 1])->toArray()[0];
                self::log('Ping successful: ' . json_encode($result));
                return;
            } catch (MongoException $e) {
                self::log("Ping attempt {$attempt}/" . self::MAX_RETRIES . " failed: {$e->getMessage()}", 'WARN');
                if ($attempt < self::MAX_RETRIES) {
                    sleep(self::RETRY_DELAY_S);
                }
            }
        }

        self::log('All ' . self::MAX_RETRIES . ' ping attempts failed. Will retry at next interval.', 'ERROR');
    }

    private static function parseInterval(string $interval): int
    {
        if (!preg_match('/^(\d+)\s*([hms])$/i', trim($interval), $matches)) {
            throw new \InvalidArgumentException(
                "Invalid interval \"{$interval}\". Use e.g. \"12h\", \"30m\", or \"60s\"."
            );
        }

        $value = (int) $matches[1];
        $unit = strtolower($matches[2]);

        return match ($unit) {
            'h' => $value * 3600,
            'm' => $value * 60,
            's' => $value,
        };
    }

    private static function log(string $message, string $level = 'INFO'): void
    {
        $timestamp = date('c');
        echo "[{$timestamp}] [{$level}] [mongo-keepalive] {$message}" . PHP_EOL;
    }
}
