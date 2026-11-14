package com.oass.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for HashUtil (passwords and file hashes). */
class HashUtilTest {

    @Test
    void sha256_knownValue() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HashUtil.sha256Hex("abc".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void sameFile_sameHash_differentFile_differentHash() {
        byte[] a = "report v1".getBytes(StandardCharsets.UTF_8);
        byte[] b = "report v2".getBytes(StandardCharsets.UTF_8);
        assertEquals(HashUtil.sha256Hex(a), HashUtil.sha256Hex(a.clone()));
        assertNotEquals(HashUtil.sha256Hex(a), HashUtil.sha256Hex(b));
    }

    @Test
    void passwordHash_isSaltedAndVerifies() {
        String salt = HashUtil.generateSalt();
        String hash = HashUtil.hash(salt, "pass123");
        assertEquals(64, hash.length());
        assertTrue(HashUtil.verify(salt, "pass123", hash));
        assertFalse(HashUtil.verify(salt, "wrong", hash));
        assertNotEquals(hash, HashUtil.hash(HashUtil.generateSalt(), "pass123"));
    }
}
