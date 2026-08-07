package com.ai.career.job.service;

import com.ai.career.job.dto.JobDto;

import java.util.List;

public interface JobIngestionService {
    int ingestJobs(List<JobDto> jobs);
    int triggerJobFetch(String keywords, String location);
}
