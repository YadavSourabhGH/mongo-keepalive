# frozen_string_literal: true

require "logger"
require "mongo"

# Keep MongoDB Atlas free-tier clusters alive by periodically sending a ping command.
module MongoKeepAlive
  DEFAULT_INTERVAL = "12h"
  MAX_RETRIES = 3
  RETRY_DELAY = 5

  # Control handle returned by {.start_keep_alive}.
  class Handle
    def initialize(client, thread)
      @client = client
      @thread = thread
      @stopped = false
    end

    # Stop the keep-alive loop and close the connection.
    def stop
      @stopped = true
      @thread.kill
      @client.close
      MongoKeepAlive.logger.info("[mongo-keepalive] Stopped and disconnected.")
    end

    def stopped?
      @stopped
    end
  end

  class << self
    # @return [Logger]
    def logger
      @logger ||= Logger.new($stdout, progname: "mongo-keepalive")
    end

    attr_writer :logger

    # Start the keep-alive loop in a background thread.
    #
    # @param uri [String] MongoDB connection string.
    # @param interval [String] Ping interval, e.g. "12h", "30m", "60s".
    # @return [Handle] A handle with a {Handle#stop} method.
    def start_keep_alive(uri:, interval: DEFAULT_INTERVAL)
      raise ArgumentError, "A MongoDB connection URI is required." if uri.nil? || uri.empty?

      interval_s = parse_interval(interval)
      logger.info("[mongo-keepalive] Starting with interval #{interval} (#{interval_s} s)")

      client = Mongo::Client.new(uri)
      db = client.use("admin").database

      # Initial ping
      ping_with_retry(db)

      thread = Thread.new do
        loop do
          sleep(interval_s)
          ping_with_retry(db)
        end
      end

      Handle.new(client, thread)
    end

    private

    def parse_interval(interval)
      match = interval.strip.match(/\A(\d+)\s*([hms])\z/i)
      raise ArgumentError, "Invalid interval \"#{interval}\". Use e.g. \"12h\", \"30m\", or \"60s\"." unless match

      value = match[1].to_i
      unit = match[2].downcase

      case unit
      when "h" then value * 3600
      when "m" then value * 60
      when "s" then value
      end
    end

    def ping_with_retry(db)
      MAX_RETRIES.times do |i|
        attempt = i + 1
        begin
          result = db.command(ping: 1)
          logger.info("[mongo-keepalive] Ping successful: #{result.documents.first}")
          return
        rescue Mongo::Error => e
          logger.warn("[mongo-keepalive] Ping attempt #{attempt}/#{MAX_RETRIES} failed: #{e.message}")
          sleep(RETRY_DELAY) if attempt < MAX_RETRIES
        end
      end
      logger.error("[mongo-keepalive] All #{MAX_RETRIES} ping attempts failed. Will retry at next interval.")
    end
  end
end
