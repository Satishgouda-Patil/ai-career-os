package com.ai.career.tracking.followup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateFollowUpRequest {
    private Integer sequenceNumber;
    private String customNotes;
}
