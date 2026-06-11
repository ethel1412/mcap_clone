package nic.meg.mcap.services;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Service
public class RateLimitingService {

	// Store buckets in memory, using IP address as the key
	private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

	/**
	 * Resolves the bucket for a given IP address. If one doesn't exist, it creates
	 * a new one.
	 */
	public Bucket resolveBucket(String ipAddress) {
		return cache.computeIfAbsent(ipAddress, this::newBucket);
	}

	/**
	 * Defines the rate limit rules. Limits to 5 requests per minute, refilling 1
	 * token every 12 seconds.
	 */
	private Bucket newBucket(String ipAddress) {
		Refill refill = Refill.intervally(10, Duration.ofMinutes(1)); // Refill 5 tokens per minute
		Bandwidth limit = Bandwidth.classic(10, refill); // Max capacity of 5

		return Bucket.builder().addLimit(limit).build();
	}
}