package com.oass.util;

/**
 * Unchecked exception wrapping SQLException from the Db helper.
 * Keeps DAO method signatures clean (no checked exceptions).
 * Syllabus: Section A (OOP, exception handling).
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
