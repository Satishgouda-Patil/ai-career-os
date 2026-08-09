package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionCapabilities {
    private boolean supportsDryRun;
    private boolean supportsFormDiscovery;
    private boolean supportsDocumentUpload;
    private boolean supportsVerification;
}
