package com.oass.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date/time helpers used by servlets and model display fields.
 * All times are in Asia/Kolkata (machine local); no UTC conversion.
 * Syllabus: Section A (java.time API).
 */
public final class TimeUtil {

    private TimeUtil() {}

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /** Formats a LocalDateTime as "09 Oct 2026, 11:59 PM". */
    public static String format(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DISPLAY_FMT);
    }

    /**
     * Computes human-readable "time left" text and a CSS class for urgency.
     * Returns a two-element array: [text, cssClass].
     * cssClass values: "time-normal" | "time-warning" (under 24 h) | "time-urgent" (under 3 h) | "time-overdue".
     */
    public static String[] timeLeft(LocalDateTime deadline) {
        LocalDateTime now = LocalDateTime.now();
        if (deadline == null || now.isAfter(deadline)) {
            return new String[]{"Overdue", "time-overdue"};
        }
        long totalMinutes = ChronoUnit.MINUTES.between(now, deadline);
        long hours   = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        String text;
        String css;

        if (hours >= 24) {
            long days = hours / 24;
            long remH = hours % 24;
            text = days + "d " + remH + "h";
            css  = "time-normal";
        } else if (hours >= 3) {
            text = hours + "h " + minutes + "m";
            css  = "time-warning";
        } else {
            text = hours + "h " + minutes + "m";
            css  = "time-urgent";
        }
        return new String[]{"Due in " + text, css};
    }

    /**
     * Derives the academic term from a date.
     * July-Dec = Fall YYYY-YY; Jan-Jun = Spring YYYY.
     */
    public static String currentTerm() {
        LocalDateTime now = LocalDateTime.now();
        int year  = now.getYear();
        int month = now.getMonthValue();
        if (month >= 7) {
            int shortNext = (year + 1) % 100;
            return String.format("Fall %d-%02d", year, shortNext);
        } else {
            return "Spring " + year;
        }
    }
}
