package com.ai.career.job.service.impl;

import com.ai.career.config.RabbitMQConfig;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.job.connector.JobFetcher;
import com.ai.career.job.dto.JobDto;
import com.ai.career.job.event.JobsFetchedEvent;
import com.ai.career.job.service.JobIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobIngestionServiceImpl implements JobIngestionService {

    private final JobRepository jobRepository;
    private final List<JobFetcher> jobFetchers;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:job.exchange}")
    private String exchangeName;

    @Override
    @Transactional
    public int ingestJobs(List<JobDto> jobs) {
        List<Long> savedJobIds = new ArrayList<>();

        for (JobDto dto : jobs) {
            // Deduplication Check 1: Unique Source & SourceJobID
            if (jobRepository.existsBySourceAndSourceJobId(dto.getSource(), dto.getSourceJobId())) {
                log.debug("Skipping duplicate job from source {}: {}", dto.getSource(), dto.getSourceJobId());
                continue;
            }

            // Deduplication Check 2: Unique Title, Company, Location
            if (dto.getTitle() != null && dto.getCompany() != null && dto.getLocation() != null &&
                jobRepository.existsByTitleAndCompanyAndLocation(dto.getTitle(), dto.getCompany(), dto.getLocation())) {
                log.debug("Skipping duplicate job posting: {} at {}", dto.getTitle(), dto.getCompany());
                continue;
            }

            Job job = Job.builder()
                .source(dto.getSource())
                .sourceJobId(dto.getSourceJobId())
                .title(dto.getTitle())
                .company(dto.getCompany())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .url(dto.getUrl())
                .postedAt(dto.getPostedAt())
                .build();

            Job savedJob = jobRepository.save(job);
            savedJobIds.add(savedJob.getId());
        }

        if (!savedJobIds.isEmpty()) {
            JobsFetchedEvent event = JobsFetchedEvent.builder()
                .source("INGESTION_PIPELINE")
                .jobIds(savedJobIds)
                .count(savedJobIds.size())
                .build();

            try {
                rabbitTemplate.convertAndSend(exchangeName, RabbitMQConfig.ROUTING_KEY_JOB_FETCHED, event);
                log.info("Published JobsFetchedEvent to RabbitMQ with {} new job IDs", savedJobIds.size());
            } catch (Exception ex) {
                log.warn("RabbitMQ server offline/unreachable. Proceeded without message publishing: {}", ex.getMessage());
            }
        }

        return savedJobIds.size();
    }

    @Override
    public int triggerJobFetch(String keywords, String location) {
        int totalIngested = 0;
        for (JobFetcher fetcher : jobFetchers) {
            log.info("Triggering job fetcher for source: {}", fetcher.getSource());
            List<JobDto> fetched = fetcher.fetchJobs(keywords, location);
            totalIngested += ingestJobs(fetched);
        }
        return totalIngested;
    }
}
