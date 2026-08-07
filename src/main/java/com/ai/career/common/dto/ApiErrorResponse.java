package com.ai.career.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String error;
    private String code;
    private int status;
    private List<String> details;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
