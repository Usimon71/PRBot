package ru.udaltsov.application;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HMACSha256Generator {
    public static String generate(String secret, String body) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        // Get an HMAC SHA-256 instance
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);

        // Compute the HMAC digest
        byte[] hmacBytes = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));

        return "sha256=" + toHexString(hmacBytes);
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
