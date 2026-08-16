package com.ai.career.execution.provider;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.config.BrowserProperties;
import com.ai.career.browser.core.PlaywrightBrowserSessionFactory;
import com.ai.career.browser.discovery.BrowserFormDiscoveryService;
import com.ai.career.browser.interaction.BrowserFormInteractor;
import com.ai.career.browser.interaction.BrowserInteractionService;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.domain.entity.Job;
import com.ai.career.execution.gate.FinalSubmissionGate;
import com.ai.career.execution.provider.ExecutionOutcomeStatus;
import com.ai.career.execution.provider.ExecutionResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class GenericFormApplicationProviderTest {

    private static HttpServer server;
    private static int port;
    private static final AtomicBoolean formSubmitted = new AtomicBoolean(false);

    private ApplicationRepository applicationRepository;
    private GenericFormApplicationProvider provider;

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    formSubmitted.set(true);
                    String confirmationHtml = "<html><body><h1>Thank you! Your application has been submitted successfully.</h1></body></html>";
                    byte[] bytes = confirmationHtml.getBytes();
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                    return;
                }

                String html = """
                        <!DOCTYPE html>
                        <html>
                        <body>
                            <form id="apply-form" action="/" method="POST">
                                <label for="fullName">Full Name</label>
                                <input type="text" id="fullName" name="fullName">

                                <label for="email">Email</label>
                                <input type="email" id="email" name="email">

                                <button type="submit">Submit Application</button>
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
        formSubmitted.set(false);

        BrowserProperties properties = new BrowserProperties();
        properties.setEnabled(true);
        properties.setHeadless(true);
        properties.setAllowLocalhost(true);

        BrowserUrlValidator validator = new BrowserUrlValidator(properties);
        PlaywrightBrowserSessionFactory factory = new PlaywrightBrowserSessionFactory(properties);
        BrowserFormDiscoveryService discoveryService = new BrowserFormDiscoveryService(validator, factory);
        BrowserFormInteractor interactor = new BrowserFormInteractor();
        FinalSubmissionGate gate = new FinalSubmissionGate();

        applicationRepository = Mockito.mock(ApplicationRepository.class);
        Job job = Job.builder().url("http://127.0.0.1:" + port + "/").build();
        Application app = Application.builder().id(200L).job(job).providerName("GENERIC_JOB_FORM").status(ApplicationState.APPROVED).build();

        Mockito.when(applicationRepository.findById(200L)).thenReturn(Optional.of(app));

        BrowserInteractionService interactionService = new BrowserInteractionService(
                applicationRepository,
                validator,
                factory,
                discoveryService,
                interactor
        );

        provider = new GenericFormApplicationProvider(
                applicationRepository,
                validator,
                factory,
                interactionService,
                interactor,
                gate
        );
    }

    @Test
    void testRejectionOnInvalidConfirmationToken() {
        ExecutionResult result = provider.execute(200L, "INVALID_TOKEN");

        assertNotNull(result);
        assertEquals(ExecutionOutcomeStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Confirmation token must be SUBMIT_APPLICATION"));
        assertFalse(formSubmitted.get(), "Form submit should not have been dispatched when confirmation token is invalid");
    }

    @Test
    void testSuccessfulExecutionAndConfirmation() {
        ExecutionResult result = provider.execute(200L, "SUBMIT_APPLICATION");

        assertNotNull(result);
        assertEquals(ExecutionOutcomeStatus.SUCCESS, result.getStatus());
        assertTrue(formSubmitted.get(), "Form submit must have been dispatched to server");
    }
}
