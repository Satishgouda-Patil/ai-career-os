package com.ai.career.browser.interaction;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.config.BrowserProperties;
import com.ai.career.browser.core.PlaywrightBrowserSessionFactory;
import com.ai.career.browser.discovery.BrowserFormDiscoveryService;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.domain.entity.Job;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.service.IntegrationAuditService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;

class BrowserInteractionServiceTest {

    private static HttpServer server;
    private static int port;
    private static final AtomicBoolean submitAttempted = new AtomicBoolean(false);

    private ApplicationRepository applicationRepository;
    private BrowserInteractionService interactionService;

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    submitAttempted.set(true);
                }
                String html = """
                        <!DOCTYPE html>
                        <html>
                        <body>
                            <form id="app-form" action="/submit" method="POST">
                                <label for="fullName">Full Name</label>
                                <input type="text" id="fullName" name="fullName">

                                <label for="email">Email</label>
                                <input type="email" id="email" name="email">

                                <label for="workAuth">Work Authorization</label>
                                <select id="workAuth" name="workAuth">
                                    <option value="YES">Yes</option>
                                    <option value="NO">No</option>
                                </select>

                                <button type="submit" id="submitBtn">Submit Application</button>
                            </form>
                        </body>
                        </html>
                        """;
                byte[] bytes = html.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        });
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void setUp() {
        submitAttempted.set(false);

        BrowserProperties properties = new BrowserProperties();
        properties.setEnabled(true);
        properties.setHeadless(true);
        properties.setAllowLocalhost(true);

        BrowserUrlValidator validator = new BrowserUrlValidator(properties);
        PlaywrightBrowserSessionFactory factory = new PlaywrightBrowserSessionFactory(properties);
        BrowserFormDiscoveryService discoveryService = new BrowserFormDiscoveryService(validator, factory);
        BrowserFormInteractor interactor = new BrowserFormInteractor();

        applicationRepository = Mockito.mock(ApplicationRepository.class);
        com.ai.career.domain.entity.User dummyUser = com.ai.career.domain.entity.User.builder().id(1L).email("test@example.com").build();
        Job job = Job.builder().url("http://127.0.0.1:" + port + "/").build();
        Application app = Application.builder().id(100L).user(dummyUser).job(job).build();

        Mockito.when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));

        DistributedExecutionLock lock = Mockito.mock(DistributedExecutionLock.class);
        Mockito.when(lock.acquire(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
        IntegrationAuditService auditService = Mockito.mock(IntegrationAuditService.class);

        interactionService = new BrowserInteractionService(
                applicationRepository,
                validator,
                factory,
                discoveryService,
                interactor,
                lock,
                auditService
        );
    }

    @Test
    void testPrepareInteractionPlan() {
        BrowserInteractionPlan plan = interactionService.prepareInteractionPlan(100L);

        assertNotNull(plan);
        assertEquals(100L, plan.getApplicationId());
        assertFalse(plan.getActions().isEmpty());

        // Sensitive field workAuth must require review
        assertTrue(plan.getReviewFields().stream().anyMatch(f -> f.contains("workAuth")));
    }

    @Test
    void testExecuteInteractionAndZeroSubmission() {
        SubmissionPreview preview = interactionService.executeInteraction(100L);

        assertNotNull(preview);
        assertEquals(100L, preview.getApplicationId());
        assertTrue(preview.isSubmitControlDetected());
        assertFalse(preview.isReadyForSubmission(), "M6-B must return readyForSubmission = false");

        // Assert ZERO submission occurred to server
        assertFalse(submitAttempted.get(), "Zero submission proof failed: submit request was sent during M6-B interaction!");
    }
}
