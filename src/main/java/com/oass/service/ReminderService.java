package com.oass.service;

import com.oass.util.Db;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Background scheduler that checks for assignments due within 2 hours.
 * Automatically dispatches:
 * 1. An in-app notification to the student
 * 2. An email alert to the student's email address
 */
public final class ReminderService {

    private static final Logger LOG = Logger.getLogger(ReminderService.class.getName());
    private static ScheduledExecutorService scheduler;

    private ReminderService() {}

    /** Starts the recurring reminder job. Runs every 2 minutes. */
    public static void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "OASS-Deadline-Reminder");
            t.setDaemon(true);
            return t;
        });

        // Run initial check after 10 seconds, then repeat every 2 minutes
        scheduler.scheduleAtFixedRate(ReminderService::checkAndSendReminders, 10, 120, TimeUnit.SECONDS);
        LOG.info("ReminderService started. Monitoring 2-hour assignment deadlines.");
    }

    /** Stops the scheduler cleanly on application shutdown. */
    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            LOG.info("ReminderService stopped.");
        }
    }

    /**
     * Checks all pending assignments where deadline is between now and now + 2 hours.
     * Prevents duplicate notifications using the reminder_log table.
     */
    public static void checkAndSendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoHoursLater = now.plusHours(2);

            // Find all students who haven't submitted an active assignment due within 2 hours
            String sql = """
                SELECT a.id AS assignment_id, a.title AS assignment_title, a.deadline,
                       sub.code AS subject_code,
                       u.id AS student_id, u.name AS student_name, u.email AS student_email
                FROM assignments a
                JOIN subjects sub ON sub.id = a.subject_id
                JOIN students st ON st.section_id = a.section_id
                JOIN users u ON u.id = st.user_id
                LEFT JOIN current_submissions cs ON cs.assignment_id = a.id AND cs.student_id = u.id
                LEFT JOIN reminder_log rl ON rl.assignment_id = a.id AND rl.student_id = u.id AND rl.stage = '2H'
                WHERE a.status = 'PUBLISHED'
                  AND a.deadline > ? AND a.deadline <= ?
                  AND cs.id IS NULL
                  AND rl.assignment_id IS NULL
                """;

            List<Map<String, Object>> pendingList = Db.query(sql, rs -> Map.of(
                    "assignmentId", rs.getInt("assignment_id"),
                    "assignmentTitle", rs.getString("assignment_title"),
                    "deadline", rs.getObject("deadline", LocalDateTime.class),
                    "subjectCode", rs.getString("subject_code"),
                    "studentId", rs.getInt("student_id"),
                    "studentName", rs.getString("student_name"),
                    "studentEmail", rs.getString("student_email")
            ), now, twoHoursLater);

            for (var item : pendingList) {
                int aid = (int) item.get("assignmentId");
                int sid = (int) item.get("studentId");
                String title = (String) item.get("assignmentTitle");
                String subject = (String) item.get("subjectCode");
                String studentName = (String) item.get("studentName");
                String studentEmail = (String) item.get("studentEmail");
                LocalDateTime deadline = (LocalDateTime) item.get("deadline");

                long minutesLeft = Duration.between(now, deadline).toMinutes();

                // 1. Insert In-App Notification
                String notifTitle = "Urgent: " + title + " due in " + minutesLeft + " mins";
                String notifMsg = "Your assignment for " + subject + " is due soon. Please submit before the deadline.";
                Db.update("INSERT INTO notifications (user_id, title, message, link, is_read) VALUES (?, ?, ?, ?, 0)",
                        sid, notifTitle, notifMsg, "/student/assignment/submit?id=" + aid);

                // 2. Send Email
                String emailSubject = "[Urgent Reminder] " + title + " deadline in " + minutesLeft + " minutes";
                String emailBody = "Dear " + studentName + ",\n\n"
                        + "This is an automated reminder that your assignment '" + title + "' (" + subject + ") is due in " + minutesLeft + " minutes.\n"
                        + "Deadline: " + deadline + "\n\n"
                        + "Please login to the portal and submit your assignment on time:\n"
                        + "http://localhost:8080/oass/student/assignment/submit?id=" + aid + "\n\n"
                        + "Regards,\nAcademic Submission Portal\nManav Rachna University";

                boolean sent = EmailService.sendEmail(studentEmail, emailSubject, emailBody);
                if (!sent) {
                    LOG.warning("Email FAILED for " + studentEmail + " - will retry on next run");
                    continue; // do NOT log, so the next 2-minute run retries
                }

                // 3. Log into reminder_log so student is never spammed twice
                Db.update("INSERT IGNORE INTO reminder_log (assignment_id, student_id, stage, deadline) VALUES (?, ?, '2H', ?)",
                        aid, sid, deadline);

                LOG.info("2-hour deadline reminder sent to " + studentEmail + " for assignment " + title);
            }

        } catch (Exception e) {
            LOG.warning("Error running ReminderService: " + e.getMessage());
        }
    }
}