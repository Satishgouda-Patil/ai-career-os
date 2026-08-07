package com.ai.career.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private String source;
    private String sourceJobId;
    private String title;
    private String company;
    private String location;
    private String description;
    private String url;
    private LocalDateTime postedAt;
}
