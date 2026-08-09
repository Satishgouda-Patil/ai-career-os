package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCapabilities {
    private Set<ProviderCapability> capabilities;
    private boolean supportsDryRun;
    private boolean supportsFormDiscovery;
    private boolean supportsDocumentUpload;
    private boolean supportsVerification;

    public boolean hasCapability(ProviderCapability capability) {
        return capabilities != null && capabilities.contains(capability);
    }
}
