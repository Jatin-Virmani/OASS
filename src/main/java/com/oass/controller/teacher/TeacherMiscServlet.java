package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.model.User;
import com.oass.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/teacher/requests", "/teacher/profile", "/teacher/notifications"
})
public class TeacherMiscServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        if (path.equals("/teacher/requests")) {
            req.setAttribute("extensions", Db.query("""
                SELECT e.*, u.name AS student_name, a.title AS assignment_title 
                FROM extensions e 
                JOIN students st ON st.user_id = e.student_id 
                JOIN users u ON u.id = st.user_id 
                JOIN assignments a ON a.id = e.assignment_id
                """, rs -> Map.of("id", rs.getInt("id"), "studentName", rs.getString("student_name"), "assignmentTitle", rs.getString("assignment_title"), "reason", rs.getString("reason") != null ? rs.getString("reason") : "Medical", "status", rs.getString("status"))));
            render(req, resp, "requests", "teacher/requests");
        } else if (path.equals("/teacher/profile")) {
            render(req, resp, "profile", "teacher/profile");
        } else if (path.equals("/teacher/notifications")) {
            render(req, resp, "notifications", "teacher/notifications");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        User user = currentUser(req);

        if (path.equals("/teacher/requests")) {
            String id = req.getParameter("id");
            String action = req.getParameter("action");
            String status = "approve".equalsIgnoreCase(action) ? "APPROVED" : "REJECTED";
            Db.update("UPDATE extensions SET status = ?, decided_by = ?, decided_at = NOW() WHERE id = ?", status, user.getId(), id);
            flash(req, "success", "Extension request " + status.toLowerCase() + "!");
            resp.sendRedirect(req.getContextPath() + "/teacher/requests");
        } else {
            flash(req, "success", "Action updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
        }
    }
}
