package com.ai.career.browser.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredOption {
    private String value;
    private String text;
    private boolean selected;
    private boolean disabled;
}
