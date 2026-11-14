package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.AnnouncementDao;
import com.oass.dao.AssignmentDao;
import com.oass.dao.NotificationDao;
import com.oass.dao.SubmissionDao;
import com.oass.dao.UserDao;
import com.oass.model.*;
import com.oass.service.SubmissionService;
import com.oass.util.Db;
import com.oass.util.TimeUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles GET /student/dashboard.
 * Computes the four stat card counts (Pending/Submitted/Late/Missed)
 * using SubmissionService.stateFor() and shows upcoming PUBLISHED assignments.
 * Syllabus: Section D (Servlet, MVC, forwarding to JSP).
 */
@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends BaseServlet {

    private final AssignmentDao  assignmentDao  = new AssignmentDao();
    private final SubmissionDao  submissionDao  = new SubmissionDao();
    private final UserDao        userDao        = new UserDao();
    private final NotificationDao notifDao      = new NotificationDao();
    private final AnnouncementDao announcementDao = new AnnouncementDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        Student student = userDao.findStudentById(user.getId());
        if (student == null) {
            // users row exists but no students row (bad/partial seed) -> clear message, not an NPE
            req.getSession().invalidate();
            req.getSession(true).setAttribute("flashError",
                    "Your student profile is missing in the database. Re-run db/seed.sql.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Include PUBLISHED + CLOSED; DRAFT is hidden. Counts all states for stat cards.
        List<Assignment> assignments = assignmentDao.listVisibleForSection(student.getSectionId());

        int pending = 0, submitted = 0, late = 0, missed = 0;
        List<Assignment> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Assignment a : assignments) {
            LocalDateTime submittedAt = submissionDao.currentSubmittedAt(user.getId(), a.getId());
            // No extension dao yet (P6) – effective deadline = assignment deadline for now
            LocalDateTime effective = SubmissionService.effectiveDeadline(a.getDeadline(), null);
            SubmissionState state = SubmissionService.stateFor(now, effective, submittedAt);

            switch (state) {
                case PENDING   -> { pending++;   if (a.getStatus() == AssignmentStatus.PUBLISHED) upcoming.add(a); }
                case SUBMITTED -> submitted++;
                case LATE      -> late++;
                case MISSED    -> missed++;
            }
        }

        // Next deadline banner: first assignment in upcoming list (already sorted by deadline)
        Assignment nextDue = upcoming.isEmpty() ? null : upcoming.get(0);

        // Recent notifications (last 5 for the sidebar widget)
        List<Notification> notifications = notifDao.listForUser(user.getId());

        int totalAssignments = pending + submitted + late + missed;
        int completionPct = totalAssignments > 0
                ? (int) Math.round(((double) (submitted + late) / totalAssignments) * 100)
                : 0;

        // Evaluated grades for academic performance visualization
        List<java.util.Map<String, Object>> grades = Db.query("""
            SELECT a.title, s.code AS subject_code, e.final_marks, a.max_marks,
                   ROUND((e.final_marks / a.max_marks) * 100, 1) AS pct
            FROM evaluations e
            JOIN submissions s_sub ON s_sub.id = e.submission_id
            JOIN assignments a ON a.id = s_sub.assignment_id
            JOIN subjects s ON s.id = a.subject_id
            WHERE s_sub.student_id = ? AND e.status IN ('FINALIZED', 'PUBLISHED')
            ORDER BY e.evaluated_at DESC
            LIMIT 4
            """, rs -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("title", rs.getString("title"));
                m.put("subjectCode", rs.getString("subject_code"));
                m.put("finalMarks", rs.getBigDecimal("final_marks"));
                m.put("maxMarks", rs.getBigDecimal("max_marks"));
                m.put("pct", rs.getDouble("pct"));
                return m;
            }, user.getId());

        double avgScore = 0;
        if (!grades.isEmpty()) {
            double sum = 0;
            for (var g : grades) {
                sum += (double) g.get("pct");
            }
            avgScore = Math.round((sum / grades.size()) * 10.0) / 10.0;
        }

        req.setAttribute("student",          student);
        req.setAttribute("pending",          pending);
        req.setAttribute("submitted",        submitted);
        req.setAttribute("late",             late);
        req.setAttribute("missed",           missed);
        req.setAttribute("totalAssignments", totalAssignments);
        req.setAttribute("completionPct",    completionPct);
        req.setAttribute("grades",           grades);
        req.setAttribute("avgScore",         avgScore);
        req.setAttribute("upcoming",         upcoming);
        req.setAttribute("nextDue",          nextDue);
        req.setAttribute("notifications",    notifications.size() > 5
                                            ? notifications.subList(0, 5)
                                            : notifications);
        req.setAttribute("announcements",    announcementDao.listForSection(student.getSectionId(), 3));
        req.setAttribute("term",             TimeUtil.currentTerm());

        render(req, resp, "dashboard", "student/dashboard");
    }
}
