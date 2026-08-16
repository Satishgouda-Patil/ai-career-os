package com.ai.career.browser.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionFieldAction {
    private String fieldId;
    private String fieldName;
    private String selector;
    private InteractionActionType actionType;
    private String value;
    private String source;
    private String reason;
}
