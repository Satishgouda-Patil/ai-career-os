package com.ai.career.execution.lock;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.execution.lock")
public class ExecutionLockProperties {
    private long leaseSeconds = 300;
    private long waitMillis = 0;
    private long retryDelayMillis = 100;
}
