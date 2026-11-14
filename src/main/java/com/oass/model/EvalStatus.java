package com.oass.model;

/**
 * Evaluation lifecycle stages stored in evaluations.status.
 * Students see marks only when status is PUBLISHED.
 * Syllabus: Section A (enums / OOP).
 */
public enum EvalStatus {
    DRAFT, FINALIZED, PUBLISHED
}
