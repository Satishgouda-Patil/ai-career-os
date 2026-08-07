package com.ai.career.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoobleRequestDto {
    private String keywords;
    private String location;
    private Integer page;
}
