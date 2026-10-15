package com.ternwire;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

public final class TernwireClient {
    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final HttpClient http;

    public TernwireClient(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, Duration.ofSeconds(30));
    }

    public TernwireClient(String apiKey, String baseUrl, Duration timeout) {
        if (apiKey == null || !apiKey.matches("em_(live|test)_[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Use an em_live_ or em_test_ API key");
        }
        URI origin = URI.create(baseUrl);
        if (!("https".equals(origin.getScheme()) || "http".equals(origin.getScheme()))
                || origin.getHost() == null || origin.getUserInfo() != null
                || !(origin.getPath().isEmpty() || origin.getPath().equals("/"))
                || origin.getRawQuery() != null || origin.getFragment() != null) {
            throw new IllegalArgumentException("baseUrl must be an HTTP(S) origin without /v1");
        }
        boolean loopback = isLoopbackHost(origin.getHost());
        if ("http".equals(origin.getScheme()) && !loopback) {
            throw new IllegalArgumentException(
                    "baseUrl must use https unless it points at localhost or a loopback address");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeout = timeout;
        HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NEVER);
        if (loopback) {
            // A proxy cannot reach this machine's loopback, and would receive the API key.
            builder.proxy(HttpClient.Builder.NO_PROXY);
        }
        this.http = builder.build();
    }

    private static boolean isLoopbackHost(String host) {
        String name = host.toLowerCase(Locale.ROOT);
        if (name.equals("localhost") || name.endsWith(".localhost")) {
            return true;
        }
        if (name.startsWith("[")) {
            // A bracketed host is parsed as an IPv6 literal, never looked up in DNS.
            try {
                return InetAddress.getByName(name).isLoopbackAddress();
            } catch (UnknownHostException e) {
                return false;
            }
        }
        return name.matches("127(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}");
    }

    private static String id(String value) {
        if (value == null || !value.matches("[a-z0-9][a-z0-9_-]{7,127}")) {
            throw new IllegalArgumentException("Invalid Ternwire resource ID");
        }
        return value;
    }

    private static String query(Object... namesAndValues) {
        var query = new StringBuilder();
        for (int index = 0; index < namesAndValues.length; index += 2) {
            Object value = namesAndValues[index + 1];
            if (value != null) {
                query.append(query.length() == 0 ? '?' : '&').append(namesAndValues[index]).append('=')
                        .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
            }
        }
        return query.toString();
    }

    // BEGIN GENERATED OPERATIONS
    public JsonObject submitEmail(JsonObject input, String idempotencyKey) throws IOException, InterruptedException {
        return request("POST", "/v1/emails", 202, Objects.requireNonNull(input), Objects.requireNonNull(idempotencyKey));
    }

    public JsonObject getMessage(String messageId) throws IOException, InterruptedException {
        return request("GET", "/v1/emails/" + id(messageId), 200, null, null);
    }

    public JsonObject createEmailTemplate(JsonObject input) throws IOException, InterruptedException {
        return request("POST", "/v1/templates", 201, Objects.requireNonNull(input), null);
    }

    public JsonObject listEmailTemplates() throws IOException, InterruptedException {
        return listEmailTemplates(null, null);
    }

    public JsonObject listEmailTemplates(String cursor, Integer limit) throws IOException, InterruptedException {
        return request("GET", "/v1/templates" + query("cursor", cursor, "limit", limit), 200, null, null);
    }

    public JsonObject getEmailTemplate(String templateId) throws IOException, InterruptedException {
        return request("GET", "/v1/templates/" + id(templateId), 200, null, null);
    }

    public JsonObject replaceEmailTemplate(String templateId, JsonObject input) throws IOException, InterruptedException {
        return request("PUT", "/v1/templates/" + id(templateId), 200, Objects.requireNonNull(input), null);
    }

    public JsonObject deleteEmailTemplate(String templateId) throws IOException, InterruptedException {
        return request("DELETE", "/v1/templates/" + id(templateId), 204, null, null);
    }

    public JsonObject createAttachmentUploadSlot(JsonObject input) throws IOException, InterruptedException {
        return request("POST", "/v1/attachments", 201, Objects.requireNonNull(input), null);
    }

    public JsonObject getAttachment(String attachmentId) throws IOException, InterruptedException {
        return request("GET", "/v1/attachments/" + id(attachmentId), 200, null, null);
    }

    public JsonObject completeAttachmentUpload(String attachmentId) throws IOException, InterruptedException {
        return request("POST", "/v1/attachments/" + id(attachmentId) + "/complete", 202, null, null);
    }

    public JsonObject createSendingDomain(JsonObject input) throws IOException, InterruptedException {
        return request("POST", "/v1/domains", 201, Objects.requireNonNull(input), null);
    }

    public JsonObject getSendingDomain(String domainId) throws IOException, InterruptedException {
        return request("GET", "/v1/domains/" + id(domainId), 200, null, null);
    }

    public JsonObject deleteSendingDomain(String domainId) throws IOException, InterruptedException {
        return request("DELETE", "/v1/domains/" + id(domainId), 202, null, null);
    }

    public JsonObject createWebhookEndpoint(JsonObject input) throws IOException, InterruptedException {
        return request("POST", "/v1/webhook-endpoints", 201, Objects.requireNonNull(input), null);
    }

    public JsonObject rotateWebhookEndpointSigningSecret(String endpointId) throws IOException, InterruptedException {
        return request("POST", "/v1/webhook-endpoints/" + id(endpointId) + "/rotate-signing-secret", 200, null, null);
    }

    public JsonObject getWebhookAttempt(String attemptId) throws IOException, InterruptedException {
        return request("GET", "/v1/webhook-attempts/" + id(attemptId), 200, null, null);
    }

    public JsonObject replayWebhookAttempt(String attemptId) throws IOException, InterruptedException {
        return request("POST", "/v1/webhook-attempts/" + id(attemptId) + "/replay", 202, null, null);
    }
    // END GENERATED OPERATIONS

    private JsonObject request(String method, String path, int expectedStatus, JsonObject body, String idempotencyKey)
            throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(timeout)
                .header("Authorization", "Bearer " + apiKey).header("Accept", "application/json");
        if (idempotencyKey != null) {
            if (!idempotencyKey.matches("[\\x21-\\x7e]{1,128}")) {
                throw new IllegalArgumentException("idempotencyKey must contain 1-128 visible ASCII characters");
            }
            request.header("Idempotency-Key", idempotencyKey);
        }
        var publisher = HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            request.header("Content-Type", "application/json");
            publisher = HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8);
        }
        var response = http.send(request.method(method, publisher).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != expectedStatus) {
            throw new TernwireApiException(response.statusCode(), response.body(), response.headers());
        }
        if (expectedStatus == 204) { return null; }
        JsonElement payload = JsonParser.parseString(response.body());
        if (!payload.isJsonObject()) {
            throw new IOException("Ternwire API returned a non-object JSON response");
        }
        return payload.getAsJsonObject();
    }
}
