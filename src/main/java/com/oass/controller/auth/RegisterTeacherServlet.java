package com.oass.controller.auth;

import com.oass.controller.BaseServlet;
import com.oass.model.User;
import com.oass.util.DataAccessException;
import com.oass.util.Db;
import com.oass.util.HashUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Handles GET /register/teacher (show form) and POST /register/teacher (create teacher account).
 * Teacher accounts are created with ACTIVE status and can log in immediately.
 */
@WebServlet("/register/teacher")
public class RegisterTeacherServlet extends BaseServlet {

    private static final Logger LOG = Logger.getLogger(RegisterTeacherServlet.class.getName());

    /** Shows the teacher registration form; redirects to dashboard if already logged in. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (currentUser(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
            return;
        }
        render(req, resp, null, "auth/register-teacher");
    }

    /** Validates and processes the teacher registration form submission. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Collect and trim parameters
        String name            = trim(req.getParameter("name"));
        String email           = trim(req.getParameter("email"));
        String phone           = trim(req.getParameter("phone"));
        String employeeId      = trim(req.getParameter("employeeId"));
        String department      = trim(req.getParameter("department"));
        String password        = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        // Server-side validation
        String validationError = validateTeacherInput(name, email, phone, employeeId, department, password, confirmPassword);
        if (validationError != null) {
            flash(req, "error", validationError);
            resp.sendRedirect(req.getContextPath() + "/register/teacher");
            return;
        }

        try {
            // Hash the password with a fresh salt
            String salt = HashUtil.generateSalt();
            String hash = HashUtil.hash(salt, password);

            // Insert into users table with ACTIVE status
            long userId = Db.insert(
                "INSERT INTO users (name, email, password_hash, salt, role, status, phone) " +
                "VALUES (?, ?, ?, ?, 'TEACHER', 'ACTIVE', ?)",
                name, email.toLowerCase(), hash, salt, phone
            );

            // Insert into teachers table
            Db.insert(
                "INSERT INTO teachers (user_id, employee_id, department) VALUES (?, ?, ?)",
                userId, employeeId, department
            );

            LOG.info("New teacher registered (ACTIVE): " + email + " (empId: " + employeeId + ")");
            flash(req, "success",
                "Teacher account registered successfully! You can now log in.");
            resp.sendRedirect(req.getContextPath() + "/login");

        } catch (DataAccessException e) {
            // Check for duplicate email or employee_id unique-key violation
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("uq_user_email") || msg.contains("duplicate entry") && msg.contains("email")) {
                flash(req, "error", "An account with this email already exists.");
            } else if (msg.contains("uq_teacher_emp") || msg.contains("duplicate entry") && msg.contains("employee")) {
                flash(req, "error", "This Employee ID is already registered.");
            } else {
                LOG.warning("Teacher registration DB error: " + e.getMessage());
                flash(req, "error", "Registration failed due to a server error. Please try again.");
            }
            resp.sendRedirect(req.getContextPath() + "/register/teacher");
        }
    }

    /** Validates all teacher registration fields; returns an error message or null if valid. */
    private String validateTeacherInput(String name, String email, String phone,
                                         String employeeId, String department,
                                         String password, String confirm) {
        if (name == null || name.isBlank())       return "Full name is required.";
        if (email == null || !email.contains("@")) return "A valid email address is required.";
        if (!email.endsWith("@mru.edu"))           return "Email must be an @mru.edu address.";
        if (phone == null || !phone.matches("\\d{10}"))  return "Phone must be exactly 10 digits.";
        if (employeeId == null || employeeId.isBlank())  return "Employee ID is required.";
        if (department == null || department.isBlank())  return "Department is required.";
        if (password == null || password.length() < 6)   return "Password must be at least 6 characters.";
        if (!password.equals(confirm))             return "Passwords do not match.";
        return null;
    }

    /** Trims a string parameter, returning null if the parameter itself is null. */
    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
