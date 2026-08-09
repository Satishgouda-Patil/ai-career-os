package com.ai.career.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);

        when(template.opsForValue()).thenReturn(valueOps);

        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                String value = invocation.getArgument(1);
                return store.putIfAbsent(key, value) == null;
            });

        when(template.hasKey(anyString()))
            .thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                return store.containsKey(key);
            });

        when(template.execute(any(DefaultRedisScript.class), anyList(), anyString()))
            .thenAnswer(invocation -> {
                List<String> keys = invocation.getArgument(1);
                String owner = invocation.getArgument(2);
                String key = keys.get(0);
                if (owner.equals(store.get(key))) {
                    store.remove(key);
                    return 1L;
                }
                return 0L;
            });

        return template;
    }
}
