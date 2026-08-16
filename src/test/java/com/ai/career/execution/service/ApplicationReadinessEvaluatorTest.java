package com.ai.career.execution.service;

import com.ai.career.application.domain.entity.*;
import com.ai.career.application.domain.repository.ApplicationApprovalRepository;
import com.ai.career.application.domain.repository.ApplicationExecutionRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.form.repository.ApplicationFormPlanRepository;
import com.ai.career.workspace.domain.entity.Workspace;
import com.ai.career.workspace.domain.repository.WorkspaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class ApplicationReadinessEvaluatorTest {

    private ApplicationRepository applicationRepository;
    private ApplicationApprovalRepository approvalRepository;
    private ApplicationExecutionRepository executionRepository;
    private ApplicationFormPlanRepository formPlanRepository;
    private WorkspaceRepository workspaceRepository;
    private ObjectMapper objectMapper;

    private ApplicationReadinessEvaluator evaluator;

    @BeforeEach
    void setUp() {
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        approvalRepository = Mockito.mock(ApplicationApprovalRepository.class);
        executionRepository = Mockito.mock(ApplicationExecutionRepository.class);
        formPlanRepository = Mockito.mock(ApplicationFormPlanRepository.class);
        workspaceRepository = Mockito.mock(WorkspaceRepository.class);
        objectMapper = new ObjectMapper();

        evaluator = new ApplicationReadinessEvaluator(
                applicationRepository,
                approvalRepository,
                executionRepository,
                formPlanRepository,
                workspaceRepository,
                objectMapper
        );
    }

    @Test
    void testReadinessForFullyPreparedApplication() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Job job = Job.builder().id(10L).title("Senior Developer").url("https://example.com/job/10").build();
        Application app = Application.builder().id(100L).user(user).job(job).status(ApplicationState.APPROVED).build();
        Workspace workspace = Workspace.builder().id(500L).user(user).job(job).build();
        ApplicationApproval approval = ApplicationApproval.builder().id(1L).application(app).action("APPROVE").approvedBy(user).build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(workspaceRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.of(workspace));
        when(approvalRepository.findByApplicationIdOrderByApprovedAtDesc(100L)).thenReturn(List.of(approval));
        when(executionRepository.findByApplicationIdOrderByCreatedAtDesc(100L)).thenReturn(Collections.emptyList());
        when(formPlanRepository.findByApplicationId(100L)).thenReturn(Optional.empty());

        ApplicationReadinessResult result = evaluator.evaluate(1L, 100L);

        assertNotNull(result);
        assertTrue(result.isReady());
        assertEquals("APPROVED", result.getCurrentState());
        assertTrue(result.getMissingArtifacts().isEmpty());
        assertTrue(result.getUnresolvedFields().isEmpty());
        assertTrue(result.getReviewReasons().isEmpty());
    }

    @Test
    void testReadinessFailureWhenWorkspaceMissing() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Job job = Job.builder().id(10L).title("Senior Developer").url("https://example.com/job/10").build();
        Application app = Application.builder().id(100L).user(user).job(job).status(ApplicationState.READY_FOR_REVIEW).build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(workspaceRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());

        ApplicationReadinessResult result = evaluator.evaluate(1L, 100L);

        assertNotNull(result);
        assertFalse(result.isReady());
        assertTrue(result.getMissingArtifacts().contains("Workspace not generated"));
    }
}
