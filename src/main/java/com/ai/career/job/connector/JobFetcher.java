package com.ai.career.job.connector;

import com.ai.career.job.dto.JobDto;

import java.util.List;

public interface JobFetcher {
    String getSource();
    List<JobDto> fetchJobs(String keywords, String location);
}
