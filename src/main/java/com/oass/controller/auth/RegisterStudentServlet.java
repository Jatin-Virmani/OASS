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
 * Handles GET /register/student (show form) and POST /register/student (create student account).
 * Validates all fields, inserts into users + students tables, then redirects to login.
 */
@WebServlet("/register/student")
public class RegisterStudentServlet extends BaseServlet {

    private static final Logger LOG = Logger.getLogger(RegisterStudentServlet.class.getName());

    /** Roll number pattern: e.g. 2K24CSUN01099 */
    private static final String ROLL_REGEX = "^2K\\d{2}[A-Z]{2,6}\\d{5}$";

    /** Shows the student registration form; redirects to dashboard if already logged in. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (currentUser(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/student/dashboard");
            return;
        }
        render(req, resp, null, "auth/register-student");
    }

    /** Validates and processes the student registration form submission. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Collect and trim parameters
        String name            = trim(req.getParameter("name"));
        String email           = trim(req.getParameter("email"));
        String phone           = trim(req.getParameter("phone"));
        String rollNo          = trim(req.getParameter("rollNo"));
        String password        = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        // Server-side validation
        String validationError = validateStudentInput(name, email, phone, rollNo, password, confirmPassword);
        if (validationError != null) {
            flash(req, "error", validationError);
            resp.sendRedirect(req.getContextPath() + "/register/student");
            return;
        }

        try {
            // Hash the password with a fresh salt
            String salt = HashUtil.generateSalt();
            String hash = HashUtil.hash(salt, password);

            // Insert into users table; get the generated user ID
            long userId = Db.insert(
                "INSERT INTO users (name, email, password_hash, salt, role, status, phone) " +
                "VALUES (?, ?, ?, ?, 'STUDENT', 'ACTIVE', ?)",
                name, email.toLowerCase(), hash, salt, phone
            );

            // Find the first available section to assign the student
            Integer sectionId = Db.queryOne(
                "SELECT id FROM sections ORDER BY id LIMIT 1",
                rs -> rs.getInt("id")
            );
            if (sectionId == null) sectionId = 1; // fallback if no sections exist yet

            // Insert into students table
            Db.insert(
                "INSERT INTO students (user_id, roll_no, section_id) VALUES (?, ?, ?)",
                userId, rollNo, sectionId
            );

            LOG.info("New student registered: " + email + " (roll: " + rollNo + ")");
            flash(req, "success", "Registration successful! You can now login.");
            resp.sendRedirect(req.getContextPath() + "/login");

        } catch (DataAccessException e) {
            // Check for duplicate email or roll_no unique-key violation
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("uq_user_email") || msg.contains("duplicate entry") && msg.contains("email")) {
                flash(req, "error", "An account with this email already exists.");
            } else if (msg.contains("uq_student_roll") || msg.contains("duplicate entry") && msg.contains("roll")) {
                flash(req, "error", "This roll number is already registered.");
            } else {
                LOG.warning("Student registration DB error: " + e.getMessage());
                flash(req, "error", "Registration failed due to a server error. Please try again.");
            }
            resp.sendRedirect(req.getContextPath() + "/register/student");
        }
    }

    /** Validates all student registration fields; returns an error message or null if valid. */
    private String validateStudentInput(String name, String email, String phone,
                                        String rollNo, String password, String confirm) {
        if (name == null || name.isBlank())    return "Full name is required.";
        if (email == null || !email.contains("@")) return "A valid email address is required.";
        if (!email.endsWith("@mru.edu"))       return "Email must be an @mru.edu address.";
        if (phone == null || !phone.matches("\\d{10}")) return "Phone must be exactly 10 digits.";
        if (rollNo == null || !rollNo.matches(ROLL_REGEX)) return "Roll No must match format like 2K24CSUN01099.";
        if (password == null || password.length() < 6)    return "Password must be at least 6 characters.";
        if (!password.equals(confirm))         return "Passwords do not match.";
        return null;
    }

    /** Trims a string parameter, returning null if the parameter itself is null. */
    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
