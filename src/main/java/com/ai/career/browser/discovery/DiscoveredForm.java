package com.ai.career.browser.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredForm {
    private String id;
    private String action;
    private String method;

    @Builder.Default
    private List<DiscoveredField> fields = new ArrayList<>();

    @Builder.Default
    private List<String> buttons = new ArrayList<>();
}
