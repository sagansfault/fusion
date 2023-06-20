package com.projecki.fusion.util.simplelock;

import java.util.concurrent.CompletableFuture;

public interface SimpleLock {

    /**
     * Attempts to acquire the lock in the child implementation if it is not already locked. This function will
     * check if the lock is locked, and only lock it if it was not locked already. All these operations/checks
     * happen sequentially with nothing in between.
     *
     * @return A future that completes with the status of the acquired lock. True if the lock was acquired, false if not.
     */
    CompletableFuture<Boolean> attemptAcquireLock();

    /**
     * Unlocks the acquired lock. The logic for this depends on the child implementation. For instance, the redis
     * implementation will only release the lock if it was originally set by this application.
     *
     * @return A future that completes with the status of the unlocked lock. True if the lock was unlocked, false if not.
     */
    CompletableFuture<Boolean> attemptUnlock();
}
