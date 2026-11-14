package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.dao.AssignmentDao;
import com.oass.dao.EvaluationDao;
import com.oass.dao.SubmissionDao;
import com.oass.dao.UserDao;
import com.oass.model.Assignment;
import com.oass.model.User;
import com.oass.util.Db;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles GET /teacher/dashboard.
 * Computes assignment metrics, submission turnout rates,
 * plagiarism risk distribution, and recent assignments.
 * Syllabus: Section D (Servlet, MVC, forwarding to JSP).
 */
@WebServlet("/teacher/dashboard")
public class TeacherDashboardServlet extends BaseServlet {

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final SubmissionDao submissionDao = new SubmissionDao();
    private final UserDao       userDao       = new UserDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);

        List<Assignment> assignments = assignmentDao.listByTeacher(user.getId());

        int totalAssignments = assignments.size();
        int totalSubmissions = 0;
        for (Assignment a : assignments) {
            totalSubmissions += submissionDao.countByAssignment(a.getId());
        }

        // 5 most recent assignments
        List<Assignment> recent = assignments.size() > 5 ? assignments.subList(0, 5) : assignments;

        // Class submission turnout statistics for visualization
        List<Map<String, Object>> submissionStats = Db.query("""
            SELECT a.id, a.title, s.code AS subject_code,
                   (SELECT COUNT(*) FROM submissions sub WHERE sub.assignment_id = a.id AND sub.upload_status = 'ACCEPTED') AS sub_count,
                   (SELECT COUNT(*) FROM students st WHERE st.section_id = a.section_id) AS total_students,
                   (SELECT AVG(e.final_marks) FROM evaluations e JOIN submissions s2 ON s2.id = e.submission_id
                     WHERE s2.assignment_id = a.id AND e.status = 'PUBLISHED') AS avg_marks
            FROM assignments a
            JOIN subjects s ON s.id = a.subject_id
            WHERE a.teacher_id = ?
            ORDER BY a.created_at DESC
            LIMIT 4
            """, rs -> {
                int subCount = rs.getInt("sub_count");
                int totalStudents = rs.getInt("total_students");
                int pct = totalStudents > 0 ? (int) Math.round(((double) subCount / totalStudents) * 100) : 0;
                Map<String, Object> m = new HashMap<>();
                m.put("title", rs.getString("title"));
                m.put("subjectCode", rs.getString("subject_code"));
                m.put("subCount", subCount);
                m.put("totalStudents", totalStudents);
                m.put("pct", pct);
                java.math.BigDecimal avg = rs.getBigDecimal("avg_marks");
                m.put("avgMarks", avg == null ? null : avg.setScale(1, java.math.RoundingMode.HALF_UP));
                return m;
            }, user.getId());

        // Plagiarism risk analysis statistics
        Map<String, Integer> riskCounts = new HashMap<>();
        riskCounts.put("LOW", 0);
        riskCounts.put("MEDIUM", 0);
        riskCounts.put("HIGH", 0);

        Db.query("SELECT risk, COUNT(*) AS count FROM similarity_results GROUP BY risk", rs -> {
            riskCounts.put(rs.getString("risk"), rs.getInt("count"));
            return null;
        });

        int totalPairs = riskCounts.get("LOW") + riskCounts.get("MEDIUM") + riskCounts.get("HIGH");
        int lowPct = totalPairs > 0 ? (int) Math.round(((double) riskCounts.get("LOW") / totalPairs) * 100) : 0;
        int highPct = totalPairs > 0 ? (int) Math.round(((double) riskCounts.get("HIGH") / totalPairs) * 100) : 0;
        int medPct = totalPairs > 0 ? Math.max(0, 100 - lowPct - highPct) : 0;

        // average marks (in %) over all published evaluations of this teacher's assignments
        Double avgPercent = evaluationDao.averagePercentForTeacher(user.getId());
        req.setAttribute("avgMarksText", avgPercent == null ? "-" : String.format("%.1f%%", avgPercent));

        req.setAttribute("totalAssignments", totalAssignments);
        req.setAttribute("totalSubmissions", totalSubmissions);
        req.setAttribute("recentAssignments", recent);
        req.setAttribute("submissionStats",  submissionStats);
        req.setAttribute("riskCounts",       riskCounts);
        req.setAttribute("totalPairs",       totalPairs);
        req.setAttribute("lowPct",           lowPct);
        req.setAttribute("highPct",          highPct);
        req.setAttribute("medPct",           medPct);
        req.setAttribute("unreadCount",      userDao.countUnread(user.getId()));

        render(req, resp, "dashboard", "teacher/dashboard");
    }
}
