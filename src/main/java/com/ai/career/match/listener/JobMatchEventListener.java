package com.ai.career.match.listener;

import com.ai.career.job.event.JobsFetchedEvent;
import com.ai.career.match.service.MatchScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobMatchEventListener {

    private final MatchScoringService matchScoringService;

    @RabbitListener(queues = "${app.rabbitmq.queue.match:job.match.queue}")
    public void handleJobsFetchedEvent(JobsFetchedEvent event) {
        log.info("Received JobsFetchedEvent from RabbitMQ queue with {} jobs", event.getCount());
        if (event.getJobIds() != null && !event.getJobIds().isEmpty()) {
            matchScoringService.processJobMatching(event.getJobIds());
        }
    }
}
