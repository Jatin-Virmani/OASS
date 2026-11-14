package com.oass.model;

/**
 * A student's submission state for one assignment.
 * Computed by SubmissionService.stateFor(); not stored in the DB.
 * Syllabus: Section A (enums / OOP).
 */
public enum SubmissionState {
    PENDING,    // no submission, deadline in the future
    SUBMITTED,  // current submission is on time
    LATE,       // current submission is after the effective deadline
    MISSED      // no submission, deadline has passed
}
