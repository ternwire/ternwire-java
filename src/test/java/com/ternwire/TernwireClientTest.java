package com.ternwire;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class TernwireClientTest {
    private static final String KEY = "em_test_abc123";
    private static final String MESSAGE = "{\"id\":\"msg_12345678\"}";

    @Test
    void clientWithoutOriginCallsProductionApi() throws Exception {
        var transport = new RecordingTransport(MESSAGE);
        var client = new TernwireClient(KEY, Duration.ofSeconds(5), transport);

        client.getMessage("msg_12345678");

        assertEquals(List.of(URI.create("https://ternwire.com/v1/emails/msg_12345678")), transport.uris);
    }

    @Test
    void keyOnlyConstructorsAcceptTheDefaultOrigin() {
        assertEquals("https://ternwire.com", TernwireClient.DEFAULT_BASE_URL);
        assertDoesNotThrow(() -> new TernwireClient(KEY));
        assertDoesNotThrow(() -> new TernwireClient(KEY, Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class, () -> new TernwireClient("sk_live_abc"));
        assertThrows(IllegalArgumentException.class, () -> new TernwireClient(KEY, Duration.ZERO));
    }

    @Test
    void explicitOriginWinsOverTheDefault() throws Exception {
        var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        var seen = new ArrayList<String>();
        server.createContext("/", exchange -> {
            seen.add(exchange.getRequestHeaders().getFirst("Host") + exchange.getRequestURI());
            byte[] body = MESSAGE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String host = "127.0.0.1:" + server.getAddress().getPort();
            var client = new TernwireClient(KEY, "http://" + host);

            client.getMessage("msg_12345678");

            assertEquals(List.of(host + "/v1/emails/msg_12345678"), seen);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void explicitNullOrEmptyOriginIsStillRejected() {
        assertThrows(NullPointerException.class, () -> new TernwireClient(KEY, (String) null));
        assertThrows(IllegalArgumentException.class, () -> new TernwireClient(KEY, ""));
    }

    private record Response<T>(int statusCode, HttpRequest request, T body) implements HttpResponse<T> {
        public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
        public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (name, value) -> true); }
        public Optional<SSLSession> sslSession() { return Optional.empty(); }
        public URI uri() { return request.uri(); }
        public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }

    private static final class RecordingTransport extends HttpClient {
        final List<URI> uris = new ArrayList<>();
        private final String body;

        RecordingTransport(String body) { this.body = body; }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
            uris.add(request.uri());
            return new Response<>(200, request, (T) body);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request, HttpResponse.BodyHandler<T> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> handler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { throw new UnsupportedOperationException(); }
        @Override public SSLParameters sslParameters() { throw new UnsupportedOperationException(); }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
    }
}
