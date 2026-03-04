// Package keepalive keeps MongoDB Atlas free-tier clusters alive
// by periodically sending a ping command.
package keepalive

import (
	"context"
	"fmt"
	"log"
	"regexp"
	"strconv"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

const (
	defaultInterval = "12h"
	maxRetries      = 3
	retryDelay      = 5 * time.Second
)

// Options configures the keep-alive behaviour.
type Options struct {
	// URI is the MongoDB connection string (required).
	URI string
	// Interval is the ping interval, e.g. "12h", "30m", "60s". Defaults to "12h".
	Interval string
}

// Handle provides a way to stop the keep-alive loop.
type Handle struct {
	cancel context.CancelFunc
	client *mongo.Client
	done   chan struct{}
}

// Stop cancels the keep-alive loop and disconnects from MongoDB.
func (h *Handle) Stop() error {
	h.cancel()
	<-h.done
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	log.Println("[mongo-keepalive] Stopped and disconnected.")
	return h.client.Disconnect(ctx)
}

// parseInterval converts a string like "12h", "30m", or "60s" to a time.Duration.
func parseInterval(interval string) (time.Duration, error) {
	re := regexp.MustCompile(`(?i)^(\d+)\s*([hms])$`)
	matches := re.FindStringSubmatch(strings.TrimSpace(interval))
	if matches == nil {
		return 0, fmt.Errorf("invalid interval %q: use e.g. \"12h\", \"30m\", or \"60s\"", interval)
	}
	value, _ := strconv.Atoi(matches[1])
	switch strings.ToLower(matches[2]) {
	case "h":
		return time.Duration(value) * time.Hour, nil
	case "m":
		return time.Duration(value) * time.Minute, nil
	case "s":
		return time.Duration(value) * time.Second, nil
	}
	return 0, fmt.Errorf("unknown unit in interval %q", interval)
}

// pingWithRetry sends a ping command, retrying on failure.
func pingWithRetry(ctx context.Context, db *mongo.Database) {
	for attempt := 1; attempt <= maxRetries; attempt++ {
		var result bson.M
		err := db.RunCommand(ctx, bson.D{{Key: "ping", Value: 1}}).Decode(&result)
		if err == nil {
			log.Printf("[mongo-keepalive] Ping successful: %v\n", result)
			return
		}
		log.Printf("[mongo-keepalive] Ping attempt %d/%d failed: %v\n", attempt, maxRetries, err)
		if attempt < maxRetries {
			time.Sleep(retryDelay)
		}
	}
	log.Printf("[mongo-keepalive] All %d ping attempts failed. Will retry at next interval.\n", maxRetries)
}

// StartKeepAlive begins periodically pinging MongoDB.
// It returns a Handle that can be used to stop the loop.
func StartKeepAlive(opts Options) (*Handle, error) {
	if opts.URI == "" {
		return nil, fmt.Errorf("[mongo-keepalive] a MongoDB connection URI is required")
	}

	interval := opts.Interval
	if interval == "" {
		interval = defaultInterval
	}

	dur, err := parseInterval(interval)
	if err != nil {
		return nil, err
	}

	log.Printf("[mongo-keepalive] Starting with interval %s (%v)\n", interval, dur)

	ctx, cancel := context.WithCancel(context.Background())

	clientOpts := options.Client().ApplyURI(opts.URI)
	client, err := mongo.Connect(ctx, clientOpts)
	if err != nil {
		cancel()
		return nil, fmt.Errorf("[mongo-keepalive] connection failed: %w", err)
	}

	db := client.Database("admin")

	// Initial ping
	pingWithRetry(ctx, db)

	done := make(chan struct{})

	go func() {
		defer close(done)
		ticker := time.NewTicker(dur)
		defer ticker.Stop()
		for {
			select {
			case <-ctx.Done():
				return
			case <-ticker.C:
				pingWithRetry(ctx, db)
			}
		}
	}()

	return &Handle{cancel: cancel, client: client, done: done}, nil
}
