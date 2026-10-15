package com.ternwire;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.http.HttpHeaders;

public final class TernwireApiException extends RuntimeException {
    private final int status;
    private final String rawBody;
    private final JsonElement body;
    private final HttpHeaders headers;

    public TernwireApiException(int status, String rawBody, HttpHeaders headers) {
        super("Ternwire API returned HTTP " + status);
        this.status = status;
        this.rawBody = rawBody;
        this.headers = headers;
        JsonElement parsed;
        try { parsed = JsonParser.parseString(rawBody); }
        catch (RuntimeException ignored) { parsed = null; }
        this.body = parsed;
    }

    public int status() { return status; }
    public String rawBody() { return rawBody; }
    public JsonElement body() { return body; }
    public HttpHeaders headers() { return headers; }
}
