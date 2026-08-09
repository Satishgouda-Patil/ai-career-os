package com.ai.career.execution.lock;

public interface DistributedExecutionLock {
    boolean acquire(String lockKey, String ownerId, long leaseSeconds);
    boolean release(String lockKey, String ownerId);
    boolean isHeld(String lockKey);
}
