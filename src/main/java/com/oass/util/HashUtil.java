package com.oass.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing and verification using SHA-256.
 * Rule: password_hash = lowercase hex of SHA-256(salt + password).
 * Salt is 16 random bytes encoded as Base64.
 * Syllabus: Section A (Java security APIs).
 */
public final class HashUtil {

    private HashUtil() {}

    /** Generates a new random salt (16 bytes, Base64-encoded). */
    public static String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /** Hashes (salt + password) with SHA-256 and returns the lowercase hex string. */
    public static String hash(String salt, String password) {
        return sha256Hex((salt + password).getBytes(StandardCharsets.UTF_8));
    }

    /** SHA-256 of any bytes (passwords, uploaded files) as a 64-character lowercase hex string. */
    public static String sha256Hex(byte[] data) {
        try {
            byte[] raw = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder sb = new StringBuilder(64);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Returns true if the provided password matches the stored hash. */
    public static boolean verify(String salt, String password, String storedHash) {
        byte[] computed = hexToBytes(hash(salt, password));
        byte[] stored   = hexToBytes(storedHash);
        return MessageDigest.isEqual(computed, stored);
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
