package com.ai.career.browser.discovery;

import com.ai.career.browser.config.BrowserProperties;
import com.ai.career.browser.core.PlaywrightBrowserSessionFactory;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ZeroSubmissionGuaranteeTest {

    private static HttpServer server;
    private static int port;
    private static final AtomicBoolean postReceived = new AtomicBoolean(false);
    private BrowserFormDiscoveryService discoveryService;

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    postReceived.set(true);
                }
                String html = """
                        <!DOCTYPE html>
                        <html>
                        <body>
                            <form id="test-form" action="/" method="POST">
                                <label for="name">Name</label>
                                <input type="text" id="name" name="name">
                                <button type="submit">Submit</button>
                            </form>
                            <script>
                                window.submitted = false;
                                document.getElementById('test-form').addEventListener('submit', function(e) {
                                    window.submitted = true;
                                });
                            </script>
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
        postReceived.set(false);
        BrowserProperties properties = new BrowserProperties();
        properties.setEnabled(true);
        properties.setHeadless(true);
        properties.setAllowLocalhost(true);

        BrowserUrlValidator validator = new BrowserUrlValidator(properties);
        PlaywrightBrowserSessionFactory factory = new PlaywrightBrowserSessionFactory(properties);
        discoveryService = new BrowserFormDiscoveryService(validator, factory);
    }

    @Test
    void testZeroSubmissionGuarantee() {
        String url = "http://127.0.0.1:" + port + "/";
        BrowserDiscoveryResult result = discoveryService.discoverForms(url);

        assertNotNull(result);
        assertEquals(1, result.getForms().size());

        // Assert no HTTP POST request was dispatched to the server
        assertFalse(postReceived.get(), "Zero submission guarantee failed: HTTP POST request was made during form discovery!");
    }
}
