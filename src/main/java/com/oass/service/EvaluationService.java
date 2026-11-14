package com.oass.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Marks rules used when a teacher evaluates a submission.
 * Pure methods (no database) so they can be unit tested.
 */
public class EvaluationService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /**
     * Late penalty in MARKS.
     * Every started day after the deadline costs pctPerDay % of the total, up to maxPct %.
     * (maxPct = 0 means "no cap set", so at most 100 % is taken.)
     */
    public static BigDecimal latePenalty(BigDecimal total, LocalDateTime submittedAt, LocalDateTime deadline,
                                         BigDecimal pctPerDay, BigDecimal maxPct) {
        BigDecimal none = BigDecimal.ZERO.setScale(2);
        if (submittedAt == null || deadline == null || !submittedAt.isAfter(deadline)) return none;
        if (pctPerDay == null || pctPerDay.signum() <= 0) return none;

        long minutesLate = Duration.between(deadline, submittedAt).toMinutes();
        long daysLate = Math.max(1, (minutesLate + 1439) / 1440);   // any part of a day counts as a full day

        BigDecimal cap = (maxPct == null || maxPct.signum() <= 0) ? HUNDRED : maxPct;
        BigDecimal pct = pctPerDay.multiply(BigDecimal.valueOf(daysLate)).min(cap);
        return total.multiply(pct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Final marks = total - penalty, never below zero. */
    public static BigDecimal finalMarks(BigDecimal total, BigDecimal penalty) {
        return total.subtract(penalty).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
