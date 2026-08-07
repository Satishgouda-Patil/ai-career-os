package com.ai.career.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoobleResponseDto {
    private Integer totalCount;
    private List<JoobleJobItem> jobs;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JoobleJobItem {
        private String id;
        private String title;
        private String location;
        private String snippet;
        private String company;
        private String link;
        private String updated;
    }
}
