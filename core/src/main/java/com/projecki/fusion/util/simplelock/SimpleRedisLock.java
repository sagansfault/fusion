package com.projecki.fusion.util.simplelock;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimpleRedisLock implements SimpleLock {

    private final String key;
    private UUID uniqueValue;
    private final Duration maxLockTime;
    private final RedisAsyncCommands<String, String> redis;

    /**
     * Creates a new redis lock
     *
     * @param key The key to use for this lock, should be unique per instance
     * @param maxLockTime The maximum time a lock can be held for before it is forcibly unlocked
     * @param redis The redis commands to use
     */
    public SimpleRedisLock(String key, Duration maxLockTime, RedisAsyncCommands<String, String> redis) {
        this.key = key;
        this.maxLockTime = maxLockTime;
        this.redis = redis;
    }

    /**
     * Creates a new redis lock
     *
     * @param key The key to use for this lock, should be unique per instance
     * @param redis The redis commands to use
     */
    public SimpleRedisLock(String key, RedisAsyncCommands<String, String> redis) {
        this(key, Duration.ofSeconds(10), redis);
    }

    private synchronized void setUniqueValue(UUID uuid) {
        this.uniqueValue = uuid;
    }

    private synchronized UUID getUniqueValue() {
        return this.uniqueValue;
    }

    /**
     * Attempts to acquire this redis lock. This will only acquire the lock if it is available. This will not halt
     * any threads to wait until the lock can be acquired, it will simply check if it can acquire it and do so if it can.
     *
     * Note, all locks are automatically unlocked after a default duration time of 10 seconds or an otherwise
     * specified duration.
     *
     * @return A future that completes with whether this lock has been acquired.
     */
    @Override
    public CompletableFuture<Boolean> attemptAcquireLock() {
        UUID temp = UUID.randomUUID();
        return redis.set(key, temp.toString(), SetArgs.Builder.nx().ex(maxLockTime)).thenApply(s -> {
            boolean set = s == null;
            if (set) {
                setUniqueValue(temp);
            }
            return set;
        }).toCompletableFuture();
    }

    /**
     * Attempts to unlock this lock. This will only unlock the lock if it was set by this instance who had acquired it.
     * What this means is, if one instance of this class gains the lock and another instance attempts to unlock it, it
     * will be unsuccessful as only the original acquirer can unlock the lock.
     *
     * Note, all locks are automatically unlocked after a default duration time of 10 seconds or an otherwise
     * specified duration.
     *
     * @return A future that completes with whether this lock was unlocked or not.
     */
    @Override
    public CompletableFuture<Boolean> attemptUnlock() {
        return redis.get(key).thenCompose(s -> {
            if (s != null && s.equals(this.getUniqueValue().toString())) {
                return redis.del(key).thenApply(l -> true);
            } else {
                return CompletableFuture.completedFuture(false);
            }
        }).toCompletableFuture();
    }
}
