package com.ai.career.tracking.email;

import com.ai.career.tracking.email.dto.EmailClassificationDto;
import com.ai.career.tracking.email.service.EmailClassifierService;
import com.ai.career.tracking.email.service.impl.EmailClassifierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailClassifierTest {

    private EmailClassifierService classifierService;

    @BeforeEach
    void setUp() {
        classifierService = new EmailClassifierServiceImpl();
    }

    @Test
    void testClassifyApplicationConfirmation() {
        EmailClassificationDto result = classifierService.classify(
                "jobs@acme.com",
                "Thank you for applying to Acme AI",
                "We have received your application for Senior Software Engineer."
        );

        assertNotNull(result);
        assertEquals("APPLICATION_CONFIRMATION", result.getClassification());
        assertTrue(result.getConfidence() >= 0.90);
    }

    @Test
    void testClassifyInterviewInviteWithZoomUrl() {
        EmailClassificationDto result = classifierService.classify(
                "recruiter@acme.com",
                "Invitation to interview for Backend Developer",
                "We would like to schedule a call. Zoom link: https://zoom.us/j/123456789"
        );

        assertNotNull(result);
        assertEquals("INTERVIEW_INVITATION", result.getClassification());
        assertTrue(result.getConfidence() >= 0.90);
        assertNotNull(result.getExtractedData());
        assertEquals("https://zoom.us/j/123456789", result.getExtractedData().get("meetingUrl"));
    }

    @Test
    void testClassifyRejectionNotice() {
        EmailClassificationDto result = classifierService.classify(
                "no-reply@company.com",
                "Update on your application",
                "We regret to inform you that we are pursuing other candidates at this time."
        );

        assertNotNull(result);
        assertEquals("REJECTION", result.getClassification());
        assertTrue(result.getConfidence() >= 0.90);
    }

    @Test
    void testClassifyRecruiterResponse() {
        EmailClassificationDto result = classifierService.classify(
                "sarah@techcorp.com",
                "Follow up regarding your application",
                "Hi, I am a recruiter at TechCorp and wanted to touch base regarding next steps."
        );

        assertNotNull(result);
        assertEquals("RECRUITER_RESPONSE", result.getClassification());
        assertTrue(result.getConfidence() >= 0.75);
    }
}
