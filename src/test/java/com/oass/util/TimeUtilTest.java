package com.oass.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeUtil helper methods.
 * Tests use LocalDateTime.now() as the reference so deadlines are always
 * computed relative to the actual runtime, not a fixed constant.
 * Syllabus: Section A (java.time API), JUnit 5.
 */
class TimeUtilTest {

    @Test
    void timeLeft_normalWhenMoreThan24h() {
        // Deadline 3 days from now → time-normal
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        String[] result = TimeUtil.timeLeft(deadline);
        assertTrue(result[0].startsWith("Due in"), "Should start with 'Due in'");
        assertEquals("time-normal", result[1]);
    }

    @Test
    void timeLeft_warningWhenUnder24h() {
        // Deadline 10 hours from now → time-warning
        LocalDateTime deadline = LocalDateTime.now().plusHours(10);
        String[] result = TimeUtil.timeLeft(deadline);
        assertEquals("time-warning", result[1]);
    }

    @Test
    void timeLeft_urgentWhenUnder3h() {
        // Deadline 2h 14m from now → time-urgent
        LocalDateTime deadline = LocalDateTime.now().plusHours(2).plusMinutes(14);
        String[] result = TimeUtil.timeLeft(deadline);
        assertEquals("time-urgent", result[1]);
    }

    @Test
    void timeLeft_overdueWhenPast() {
        // Deadline 1 hour in the past → overdue
        LocalDateTime deadline = LocalDateTime.now().minusHours(1);
        String[] result = TimeUtil.timeLeft(deadline);
        assertEquals("Overdue", result[0]);
        assertEquals("time-overdue", result[1]);
    }

    @Test
    void format_returnsReadableString() {
        LocalDateTime dt = LocalDateTime.of(2026, 10, 9, 23, 59, 0);
        String text = TimeUtil.format(dt);
        assertNotNull(text);
        assertTrue(text.contains("2026"), "Should contain year");
        assertTrue(text.contains("Oct"), "Should contain month abbreviation");
    }

    @Test
    void currentTerm_hasValidFormat() {
        String term = TimeUtil.currentTerm();
        assertNotNull(term);
        assertTrue(term.startsWith("Fall") || term.startsWith("Spring"),
                "Term must start with Fall or Spring, got: " + term);
    }

    @Test
    void hashUtil_verifyRoundTrip() {
        String salt = HashUtil.generateSalt();
        String hash = HashUtil.hash(salt, "pass123");
        assertTrue(HashUtil.verify(salt, "pass123", hash), "Correct password should verify");
        assertFalse(HashUtil.verify(salt, "wrong", hash), "Wrong password should not verify");
    }
}
