package com.ai.career.execution.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RedisDistributedExecutionLockTest {

    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisDistributedExecutionLock lock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lock = new RedisDistributedExecutionLock(stringRedisTemplate);
    }

    @Test
    public void testAcquireLockSuccess() {
        when(valueOperations.setIfAbsent(eq("application-execution:100"), eq("owner-1"), any(Duration.class)))
            .thenReturn(true);

        boolean acquired = lock.acquire("application-execution:100", "owner-1", 300);
        assertTrue(acquired);
    }

    @Test
    public void testAcquireLockFailureWhenAlreadyHeld() {
        when(valueOperations.setIfAbsent(eq("application-execution:100"), eq("owner-2"), any(Duration.class)))
            .thenReturn(false);

        boolean acquired = lock.acquire("application-execution:100", "owner-2", 300);
        assertFalse(acquired);
    }

    @Test
    public void testReleaseLockSuccessForOwner() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), eq(List.of("application-execution:100")), eq("owner-1")))
            .thenReturn(1L);

        boolean released = lock.release("application-execution:100", "owner-1");
        assertTrue(released);
    }

    @Test
    public void testReleaseLockRejectedForNonOwner() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), eq(List.of("application-execution:100")), eq("owner-2")))
            .thenReturn(0L);

        boolean released = lock.release("application-execution:100", "owner-2");
        assertFalse(released);
    }

    @Test
    public void testRedisFailureFailsClosed() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenThrow(new RedisConnectionFailureException("Redis connection error"));

        boolean acquired = lock.acquire("application-execution:100", "owner-1", 300);
        assertFalse(acquired); // Fail closed safety rule
    }
}
