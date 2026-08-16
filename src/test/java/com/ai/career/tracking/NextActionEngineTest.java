package com.ai.career.tracking;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.service.NextActionDecision;
import com.ai.career.tracking.service.NextActionEngine;
import com.ai.career.tracking.service.impl.NextActionEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NextActionEngineTest {

    private NextActionEngine nextActionEngine;

    @BeforeEach
    void setUp() {
        nextActionEngine = new NextActionEngineImpl();
    }

    @Test
    void testAppliedOverdueFollowUp() {
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationState.APPLIED)
                .submittedAt(LocalDateTime.now().minusDays(5))
                .build();

        NextActionDecision decision = nextActionEngine.evaluateNextAction(app, Collections.emptyList());

        assertNotNull(decision);
        assertEquals("FOLLOW_UP_DUE", decision.getNextAction());
        assertEquals("HIGH", decision.getUrgency());
        assertTrue(decision.getReason().contains("Applied 5 days ago"));
    }

    @Test
    void testAppliedRecentNoFollowUpDueYet() {
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationState.APPLIED)
                .submittedAt(LocalDateTime.now().minusDays(1))
                .build();

        NextActionDecision decision = nextActionEngine.evaluateNextAction(app, Collections.emptyList());

        assertNotNull(decision);
        assertEquals("AWAIT_RECRUITER_RESPONSE", decision.getNextAction());
        assertEquals("LOW", decision.getUrgency());
    }

    @Test
    void testReadyForReview() {
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build();

        NextActionDecision decision = nextActionEngine.evaluateNextAction(app, Collections.emptyList());

        assertNotNull(decision);
        assertEquals("APPROVE_APPLICATION", decision.getNextAction());
        assertEquals("HIGH", decision.getUrgency());
    }

    @Test
    void testApprovedState() {
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationState.APPROVED)
                .build();

        NextActionDecision decision = nextActionEngine.evaluateNextAction(app, Collections.emptyList());

        assertNotNull(decision);
        assertEquals("DISPATCH_EXECUTION", decision.getNextAction());
    }

    @Test
    void testInterviewScheduledActivityOverridesState() {
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationState.APPLIED)
                .submittedAt(LocalDateTime.now().minusDays(10))
                .build();

        ActivityDto activity = ActivityDto.builder()
                .id(100L)
                .activityType("INTERVIEW_SCHEDULED")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        NextActionDecision decision = nextActionEngine.evaluateNextAction(app, List.of(activity));

        assertNotNull(decision);
        assertEquals("PREPARE_FOR_INTERVIEW", decision.getNextAction());
        assertEquals("HIGH", decision.getUrgency());
    }

    @Test
    void testTerminalStateRejected() {
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationState.REJECTED)
                .build();

        NextActionDecision decision = nextActionEngine.evaluateNextAction(app, Collections.emptyList());

        assertNotNull(decision);
        assertEquals("NONE", decision.getNextAction());
        assertNull(decision.getDueDate());
    }
}
