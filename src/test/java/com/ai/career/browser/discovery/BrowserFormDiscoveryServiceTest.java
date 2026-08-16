package com.ai.career.browser.discovery;

import com.ai.career.browser.config.BrowserProperties;
import com.ai.career.browser.core.PlaywrightBrowserSessionFactory;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.form.model.FieldType;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class BrowserFormDiscoveryServiceTest {

    private static HttpServer server;
    private static int port;
    private BrowserFormDiscoveryService discoveryService;

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.startsWith("/")) path = path.substring(1);
                Path file = Paths.get("src/test/resources/fixtures", path);
                if (Files.exists(file)) {
                    byte[] bytes = Files.readAllBytes(file);
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                }
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
        BrowserProperties properties = new BrowserProperties();
        properties.setEnabled(true);
        properties.setHeadless(true);
        properties.setAllowLocalhost(true);

        BrowserUrlValidator validator = new BrowserUrlValidator(properties);
        PlaywrightBrowserSessionFactory factory = new PlaywrightBrowserSessionFactory(properties);
        discoveryService = new BrowserFormDiscoveryService(validator, factory);
    }

    @Test
    void testSimpleFormDiscovery() {
        String url = "http://127.0.0.1:" + port + "/simple-form.html";
        BrowserDiscoveryResult result = discoveryService.discoverForms(url);

        assertNotNull(result);
        assertFalse(result.isCaptchaDetected());
        assertFalse(result.isLoginRequired());
        assertEquals(1, result.getForms().size());

        DiscoveredForm form = result.getForms().get(0);
        assertEquals("job-app-form", form.getId());
        assertEquals("POST", form.getMethod());

        assertEquals(4, form.getFields().size());

        DiscoveredField nameField = form.getFields().get(0);
        assertEquals("fullName", nameField.getId());
        assertEquals("Full Name", nameField.getLabel());
        assertEquals(FieldType.TEXT, nameField.getFieldType());
        assertTrue(nameField.isRequired());

        DiscoveredField emailField = form.getFields().get(1);
        assertEquals("emailAddress", emailField.getId());
        assertEquals("Email Address", emailField.getLabel());
        assertEquals(FieldType.EMAIL, emailField.getFieldType());
        assertTrue(emailField.isRequired());

        DiscoveredField fileField = form.getFields().get(3);
        assertEquals("resume", fileField.getId());
        assertEquals(FieldType.FILE, fileField.getFieldType());
    }

    @Test
    void testComplexFormDiscovery() {
        String url = "http://127.0.0.1:" + port + "/complex-form.html";
        BrowserDiscoveryResult result = discoveryService.discoverForms(url);

        assertNotNull(result);
        assertEquals(1, result.getForms().size());

        DiscoveredForm form = result.getForms().get(0);
        assertEquals(5, form.getFields().size());

        DiscoveredField textarea = form.getFields().get(0);
        assertEquals(FieldType.TEXTAREA, textarea.getFieldType());
        assertEquals("Cover Letter", textarea.getLabel());

        DiscoveredField select = form.getFields().get(1);
        assertEquals(FieldType.SELECT, select.getFieldType());
        assertEquals(4, select.getOptions().size());
    }

    @Test
    void testLabelsResolution() {
        String url = "http://127.0.0.1:" + port + "/labels.html";
        BrowserDiscoveryResult result = discoveryService.discoverForms(url);

        DiscoveredForm form = result.getForms().get(0);
        assertEquals("Explicit Label Text", form.getFields().get(0).getLabel());
        assertEquals("EXPLICIT_LABEL", form.getFields().get(0).getLabelSource());

        assertEquals("Wrapping Label Text", form.getFields().get(1).getLabel());
        assertEquals("WRAPPING_LABEL", form.getFields().get(1).getLabelSource());

        assertEquals("Aria Label Text", form.getFields().get(2).getLabel());
        assertEquals("ARIA_LABEL", form.getFields().get(2).getLabelSource());

        assertEquals("External Label Text", form.getFields().get(3).getLabel());
        assertEquals("ARIA_LABELLEDBY", form.getFields().get(3).getLabelSource());

        assertEquals("Placeholder Text", form.getFields().get(4).getLabel());
        assertEquals("PLACEHOLDER", form.getFields().get(4).getLabelSource());
    }

    @Test
    void testCaptchaDetection() {
        String url = "http://127.0.0.1:" + port + "/captcha.html";
        BrowserDiscoveryResult result = discoveryService.discoverForms(url);

        assertTrue(result.isCaptchaDetected());
        assertTrue(result.getWarnings().contains("CAPTCHA_DETECTED"));
    }

    @Test
    void testLoginDetection() {
        String url = "http://127.0.0.1:" + port + "/login.html";
        BrowserDiscoveryResult result = discoveryService.discoverForms(url);

        assertTrue(result.isLoginRequired());
        assertTrue(result.getWarnings().contains("LOGIN_REQUIRED"));
    }
}
