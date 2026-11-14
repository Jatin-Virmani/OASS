package com.oass.model;

/**
 * Account lifecycle state stored in users.status.
 * PENDING = reserved for future approval workflows; DISABLED = account deactivated.
 * Syllabus: Section A (enums / OOP).
 */
public enum UserStatus {
    PENDING, ACTIVE, DISABLED
}
