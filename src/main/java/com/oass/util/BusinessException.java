package com.oass.util;

/**
 * Unchecked exception for broken business rules (e.g. identical resubmission).
 * The message is shown as a flash error on the screen.
 * Syllabus: Section A (OOP, exception handling).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
