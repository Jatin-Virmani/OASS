package com.oass.model;

/**
 * Plagiarism risk level computed from the similarity score.
 * LOW below risk_medium_from, MEDIUM below risk_high_from, HIGH otherwise.
 * Syllabus: Section A (enums / OOP).
 */
public enum Risk {
    LOW, MEDIUM, HIGH
}
