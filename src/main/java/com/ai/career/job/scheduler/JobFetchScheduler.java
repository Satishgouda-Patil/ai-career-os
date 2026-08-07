package com.ai.career.job.scheduler;

import com.ai.career.job.service.JobIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobFetchScheduler {

    private final JobIngestionService jobIngestionService;

    // Run every 1 hour (3600000 ms), with initial delay of 10 seconds
    @Scheduled(fixedRate = 3600000, initialDelay = 10000)
    public void scheduleJobFetch() {
        log.info("Scheduled job fetch runner triggered.");
        try {
            int newJobsCount = jobIngestionService.triggerJobFetch("Java Developer", "Remote");
            log.info("Scheduled job fetch finished. Ingested {} new jobs.", newJobsCount);
        } catch (Exception e) {
            log.error("Error during scheduled job fetch", e);
        }
    }
}
