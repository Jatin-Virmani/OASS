package com.oass.model;

/**
 * Review stage of a plagiarism case stored in plagiarism_cases.status.
 * FLAGGED = auto-detected; CONFIRMED = human reviewer confirmed it.
 * Syllabus: Section A (enums / OOP).
 */
public enum CaseStatus {
    FLAGGED, UNDER_REVIEW, FAIR, CONFIRMED
}
