package com.oass.controller.auth;

import com.oass.controller.BaseServlet;
import com.oass.dao.UserDao;
import com.oass.model.Role;
import com.oass.model.User;
import com.oass.model.UserStatus;
import com.oass.util.HashUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Handles GET /login (show form) and POST /login (authenticate).
 * On success, sets session attributes and redirects to the role dashboard.
 * Syllabus: Section D (Servlet, session management, POST-Redirect-GET).
 */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Already logged in? Redirect to their dashboard
        User user = currentUser(req);
        if (user != null) {
            resp.sendRedirect(req.getContextPath() + dashboardFor(user.getRole()));
            return;
        }
        render(req, resp, null, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // Basic blank check
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            flash(req, "error", "Email and password are required.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = userDao.findByEmail(email.trim().toLowerCase());

        if (user == null || !HashUtil.verify(user.getSalt(), password, user.getPasswordHash())) {
            LOG.warning("Failed login attempt for email: " + email);
            flash(req, "error", "Invalid email or password.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (user.getStatus() == UserStatus.PENDING) {
            flash(req, "error", "Your account is pending approval.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            flash(req, "error", "Your account has been disabled. Contact your department.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Success – create session (new id on login prevents session fixation)
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = req.getSession(true);
        // password check is done; do not keep hash/salt in the session
        user.setPasswordHash(null);
        user.setSalt(null);
        session.setAttribute("user", user);
        session.setAttribute("unreadCount", userDao.countUnread(user.getId()));

        LOG.info("Login: " + user.getEmail() + " (" + user.getRole() + ")");

        resp.sendRedirect(req.getContextPath() + dashboardFor(user.getRole()));
    }

    private String dashboardFor(Role role) {
        return switch (role) {
            case STUDENT -> "/student/dashboard";
            case TEACHER -> "/teacher/dashboard";
        };
    }
}
