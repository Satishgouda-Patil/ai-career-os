package com.ai.career.browser.interaction;

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
public class BrowserInteractionPlan {
    private Long applicationId;
    private String formId;

    @Builder.Default
    private List<InteractionFieldAction> actions = new ArrayList<>();

    @Builder.Default
    private List<String> unresolvedFields = new ArrayList<>();

    @Builder.Default
    private List<String> reviewFields = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
