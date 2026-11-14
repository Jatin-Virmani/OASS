package com.oass.service;

import com.oass.model.Assignment;
import com.oass.model.SubmissionState;

import java.time.LocalDateTime;

/**
 * Business rules for student submission state.
 * stateFor() is a pure method so it can be unit tested without a database.
 * Called by the student dashboard to compute the four stat card counts.
 * Syllabus: Section D (MVC service layer).
 */
public class SubmissionService {

    /**
     * Computes the submission state for one student/assignment pair.
     *
     * @param now             the current time (passed in so tests can fix it)
     * @param effectiveDeadline the later of the assignment deadline and any approved extension
     * @param submittedAt     the time of the current submission, or null if none
     * @return PENDING | SUBMITTED | LATE | MISSED
     */
    public static SubmissionState stateFor(LocalDateTime now,
                                           LocalDateTime effectiveDeadline,
                                           LocalDateTime submittedAt) {
        boolean hasSubmission = (submittedAt != null);
        boolean deadlinePassed = now.isAfter(effectiveDeadline);

        if (!hasSubmission) {
            return deadlinePassed ? SubmissionState.MISSED : SubmissionState.PENDING;
        }
        // has a submission
        return submittedAt.isAfter(effectiveDeadline)
                ? SubmissionState.LATE
                : SubmissionState.SUBMITTED;
    }

    /**
     * Computes the effective deadline for a student:
     * the later of the assignment deadline and any approved extension deadline.
     *
     * @param assignmentDeadline the base deadline from the assignment
     * @param extensionDeadline  the approved extension deadline, or null if none
     * @return the effective deadline
     */
    public static LocalDateTime effectiveDeadline(LocalDateTime assignmentDeadline,
                                                  LocalDateTime extensionDeadline) {
        if (extensionDeadline == null) return assignmentDeadline;
        return extensionDeadline.isAfter(assignmentDeadline) ? extensionDeadline : assignmentDeadline;
    }

    /**
     * Decides whether a student may still upload right now.
     *
     * @return null if the upload is allowed, otherwise the error message to show the student
     */
    public static String checkWindow(LocalDateTime now, LocalDateTime deadline,
                                     boolean allowLate, LocalDateTime hardCutoff) {
        if (!now.isAfter(deadline)) return null;                       // before the deadline: fine
        if (!allowLate) return "The deadline has passed and late submissions are not allowed.";
        if (hardCutoff != null && now.isAfter(hardCutoff)) return "The hard cutoff has passed. Uploads are closed.";
        return null;                                                   // late but still accepted
    }
}
