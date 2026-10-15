package com.ternwire;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Webhooks {
    private static final Pattern SIGNATURE = Pattern.compile("\\s*t=([1-9][0-9]{0,11})\\s*,\\s*v1=([a-f0-9]{64})\\s*");

    private Webhooks() {}

    public static boolean verify(String header, byte[] body, String secret) {
        return verify(header, body, secret, 300);
    }

    public static boolean verify(String header, byte[] body, String secret, long toleranceSeconds) {
        if (header == null || body == null || secret == null || secret.isEmpty() || toleranceSeconds < 0) {
            return false;
        }
        var match = SIGNATURE.matcher(header);
        if (!match.matches()) { return false; }
        long timestamp = Long.parseLong(match.group(1));
        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > toleranceSeconds) { return false; }
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((match.group(1) + ".").getBytes(StandardCharsets.US_ASCII));
            byte[] expected = mac.doFinal(body);
            return MessageDigest.isEqual(expected, HexFormat.of().parseHex(match.group(2)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HmacSHA256 is unavailable", exception);
        }
    }
}
