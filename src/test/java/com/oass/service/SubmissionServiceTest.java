package com.oass.service;

import com.oass.model.SubmissionState;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SubmissionService.stateFor() and effectiveDeadline().
 * No database required – all inputs are passed as parameters.
 * Syllabus: Section A (OOP), JUnit 5.
 */
class SubmissionServiceTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 10, 18, 10, 0, 0);

    @Test
    void pending_whenNoSubmissionAndDeadlineInFuture() {
        LocalDateTime now      = BASE;
        LocalDateTime deadline = BASE.plusHours(5);
        SubmissionState state  = SubmissionService.stateFor(now, deadline, null);
        assertEquals(SubmissionState.PENDING, state);
    }

    @Test
    void missed_whenNoSubmissionAndDeadlinePassed() {
        LocalDateTime now      = BASE;
        LocalDateTime deadline = BASE.minusHours(1);
        SubmissionState state  = SubmissionService.stateFor(now, deadline, null);
        assertEquals(SubmissionState.MISSED, state);
    }

    @Test
    void submitted_whenSubmittedBeforeDeadline() {
        LocalDateTime now         = BASE;
        LocalDateTime deadline    = BASE.plusHours(2);
        LocalDateTime submittedAt = BASE.minusHours(1);   // submitted 1 hour ago
        SubmissionState state     = SubmissionService.stateFor(now, deadline, submittedAt);
        assertEquals(SubmissionState.SUBMITTED, state);
    }

    @Test
    void submitted_whenSubmittedExactlyAtDeadline() {
        // Submitted at = deadline is NOT after deadline, so SUBMITTED
        LocalDateTime now         = BASE.plusHours(3);
        LocalDateTime deadline    = BASE;
        LocalDateTime submittedAt = BASE;   // exactly at deadline
        SubmissionState state     = SubmissionService.stateFor(now, deadline, submittedAt);
        assertEquals(SubmissionState.SUBMITTED, state);
    }

    @Test
    void late_whenSubmittedAfterDeadline() {
        LocalDateTime now         = BASE;
        LocalDateTime deadline    = BASE.minusHours(2);
        LocalDateTime submittedAt = BASE.minusHours(1);   // 1 h after deadline
        SubmissionState state     = SubmissionService.stateFor(now, deadline, submittedAt);
        assertEquals(SubmissionState.LATE, state);
    }

    @Test
    void effectiveDeadline_returnsAssignmentWhenNoExtension() {
        LocalDateTime assignmentDeadline = BASE.plusDays(3);
        LocalDateTime effective = SubmissionService.effectiveDeadline(assignmentDeadline, null);
        assertEquals(assignmentDeadline, effective);
    }

    @Test
    void effectiveDeadline_returnsExtensionWhenLater() {
        LocalDateTime assignmentDeadline = BASE.plusDays(3);
        LocalDateTime extension          = BASE.plusDays(5);
        LocalDateTime effective = SubmissionService.effectiveDeadline(assignmentDeadline, extension);
        assertEquals(extension, effective);
    }

    @Test
    void effectiveDeadline_returnsAssignmentWhenExtensionIsEarlier() {
        // An extension earlier than the deadline should be ignored
        LocalDateTime assignmentDeadline = BASE.plusDays(5);
        LocalDateTime extension          = BASE.plusDays(3);
        LocalDateTime effective = SubmissionService.effectiveDeadline(assignmentDeadline, extension);
        assertEquals(assignmentDeadline, effective);
    }
}
