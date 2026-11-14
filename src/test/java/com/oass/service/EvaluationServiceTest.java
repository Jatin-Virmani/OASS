package com.oass.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the late penalty and for the upload window check. */
class EvaluationServiceTest {

    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 11, 20, 23, 59);
    private static final BigDecimal TEN = BigDecimal.TEN;

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void noPenalty_whenOnTime() {
        assertEquals(bd("0.00"), EvaluationService.latePenalty(bd("80"), DEADLINE, DEADLINE, TEN, bd("50")));
    }

    @Test
    void oneStartedDay_costsOneDayOfPenalty() {
        BigDecimal penalty = EvaluationService.latePenalty(bd("80"), DEADLINE.plusHours(3), DEADLINE, TEN, bd("50"));
        assertEquals(bd("8.00"), penalty);                       // 10 % of 80
    }

    @Test
    void twoDays_late() {
        BigDecimal penalty = EvaluationService.latePenalty(bd("80"), DEADLINE.plusHours(30), DEADLINE, TEN, bd("50"));
        assertEquals(bd("16.00"), penalty);                      // 20 % of 80
    }

    @Test
    void penalty_isCappedAtMaximum() {
        BigDecimal penalty = EvaluationService.latePenalty(bd("100"), DEADLINE.plusDays(9), DEADLINE, TEN, bd("30"));
        assertEquals(bd("30.00"), penalty);
    }

    @Test
    void noPercentPerDay_meansNoPenalty() {
        assertEquals(bd("0.00"), EvaluationService.latePenalty(bd("80"), DEADLINE.plusDays(2), DEADLINE, BigDecimal.ZERO, bd("50")));
    }

    @Test
    void finalMarks_neverBelowZero() {
        assertEquals(bd("72.00"), EvaluationService.finalMarks(bd("80"), bd("8")));
        assertEquals(bd("0.00"), EvaluationService.finalMarks(bd("5"), bd("9")));
    }

    @Test
    void window_openBeforeDeadline() {
        assertNull(SubmissionService.checkWindow(DEADLINE.minusMinutes(1), DEADLINE, false, null));
    }

    @Test
    void window_closedAfterDeadlineWhenLateNotAllowed() {
        assertNotNull(SubmissionService.checkWindow(DEADLINE.plusMinutes(1), DEADLINE, false, null));
    }

    @Test
    void window_lateAllowedUntilHardCutoff() {
        LocalDateTime cutoff = DEADLINE.plusDays(2);
        assertNull(SubmissionService.checkWindow(DEADLINE.plusDays(1), DEADLINE, true, cutoff));
        assertNotNull(SubmissionService.checkWindow(DEADLINE.plusDays(3), DEADLINE, true, cutoff));
        assertNull(SubmissionService.checkWindow(DEADLINE.plusDays(30), DEADLINE, true, null));   // no cutoff set
    }
}
