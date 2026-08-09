package com.ai.career.execution.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedExecutionLock implements DistributedExecutionLock {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String RELEASE_LOCK_LUA_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    private final DefaultRedisScript<Long> releaseScript = new DefaultRedisScript<>(RELEASE_LOCK_LUA_SCRIPT, Long.class);

    @Override
    public boolean acquire(String lockKey, String ownerId, long leaseSeconds) {
        log.debug("Requesting distributed lock for Key: [{}], OwnerId: [{}]", lockKey, ownerId);
        try {
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
                lockKey,
                ownerId,
                Duration.ofSeconds(leaseSeconds)
            );
            if (Boolean.TRUE.equals(acquired)) {
                log.info("Distributed lock ACQUIRED for Key: [{}], OwnerId: [{}], TTL: {}s", lockKey, ownerId, leaseSeconds);
                return true;
            } else {
                log.warn("Distributed lock ACQUISITION FAILED for Key: [{}], OwnerId: [{}] - Already held", lockKey, ownerId);
                return false;
            }
        } catch (Exception ex) {
            log.error("Redis UNAVAILABLE while attempting lock acquisition for Key: [{}], OwnerId: [{}]: {}", lockKey, ownerId, ex.getMessage());
            return false; // Fail closed
        }
    }

    @Override
    public boolean release(String lockKey, String ownerId) {
        log.debug("Releasing distributed lock for Key: [{}], OwnerId: [{}]", lockKey, ownerId);
        try {
            Long result = stringRedisTemplate.execute(
                releaseScript,
                Collections.singletonList(lockKey),
                ownerId
            );
            if (result != null && result > 0) {
                log.info("Distributed lock RELEASED for Key: [{}], OwnerId: [{}]", lockKey, ownerId);
                return true;
            } else {
                log.warn("Distributed lock RELEASE REJECTED for Key: [{}], OwnerId: [{}] - Lock not held by owner", lockKey, ownerId);
                return false;
            }
        } catch (Exception ex) {
            log.error("Redis UNAVAILABLE while attempting lock release for Key: [{}], OwnerId: [{}]: {}", lockKey, ownerId, ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHeld(String lockKey) {
        try {
            Boolean hasKey = stringRedisTemplate.hasKey(lockKey);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception ex) {
            log.error("Redis UNAVAILABLE while checking lock status for Key: [{}]: {}", lockKey, ex.getMessage());
            return false;
        }
    }
}
