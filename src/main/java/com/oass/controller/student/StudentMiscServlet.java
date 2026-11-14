package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.EvaluationDao;
import com.oass.dao.NotificationDao;
import com.oass.dao.UserDao;
import com.oass.model.User;
import com.oass.util.DataAccessException;
import com.oass.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {
    "/student/notifications", "/student/profile", 
    "/student/extension/request", "/student/regrade/request"
})
public class StudentMiscServlet extends BaseServlet {
    private final NotificationDao notifDao = new NotificationDao();
    private final UserDao userDao = new UserDao();
    private final EvaluationDao evalDao = new EvaluationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }
        String path = req.getServletPath();

        if (path.equals("/student/notifications")) {
            req.setAttribute("notifications", notifDao.listForUser(user.getId()));
            notifDao.markAllRead(user.getId());
            req.getSession().setAttribute("unreadCount", 0);
            render(req, resp, "notifications", "student/notifications");
        } else if (path.equals("/student/profile")) {
            req.setAttribute("student", userDao.findStudentById(user.getId()));
            render(req, resp, "profile", "student/profile");
        } else if (path.equals("/student/extension/request")) {
            String idStr = req.getParameter("id");
            if (idStr != null && !idStr.isBlank()) {
                var assignment = Db.queryOne(
                    "SELECT id, title, deadline FROM assignments WHERE id = ?",
                    rs -> {
                        java.util.Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("title", rs.getString("title"));
                        m.put("deadlineText", rs.getTimestamp("deadline").toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
                        return m;
                    }, Integer.parseInt(idStr));
                req.setAttribute("assignment", assignment);
            }
            render(req, resp, "dashboard", "student/extension-request");
        } else if (path.equals("/student/regrade/request")) {
            render(req, resp, "results", "student/regrade-request");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String path = req.getServletPath();

        if (path.equals("/student/extension/request")) {
            int assignmentId = Integer.parseInt(req.getParameter("assignmentId"));
            String reason = req.getParameter("reason");
            String newDeadline = req.getParameter("newDeadline");
            Db.update("INSERT INTO extensions (assignment_id, student_id, new_deadline, reason, status) VALUES (?, ?, ?, ?, 'REQUESTED')",
                    assignmentId, user.getId(), newDeadline, reason);
            flash(req, "success", "Deadline extension request submitted to teacher!");
            resp.sendRedirect(req.getContextPath() + "/student/dashboard");
        } else if (path.equals("/student/regrade/request")) {
            Integer evalId = idParam(req, "evaluationId");
            if (evalId == null || evalDao.findPublishedResult(evalId, user.getId()) == null) {
                notFound(resp);            // not your result, or it does not exist
                return;
            }
            String reason = req.getParameter("reason");
            try {
                Db.update("INSERT INTO regrade_requests (evaluation_id, student_id, reason, status) VALUES (?, ?, ?, 'REQUESTED')",
                        evalId, user.getId(), reason);
                flash(req, "success", "Regrade request sent successfully!");
            } catch (DataAccessException e) {
                flash(req, "error", "You have already sent a regrade request for this result.");
            }
            resp.sendRedirect(req.getContextPath() + "/student/results");
        } else if (path.equals("/student/profile")) {
            String phone = req.getParameter("phone");
            Db.update("UPDATE users SET phone = ? WHERE id = ?", phone, user.getId());
            user.setPhone(phone);
            flash(req, "success", "Profile updated successfully!");
            resp.sendRedirect(req.getContextPath() + "/student/profile");
        }
    }
}
