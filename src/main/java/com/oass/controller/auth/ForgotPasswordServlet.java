package com.oass.controller.auth;

import com.oass.controller.BaseServlet;
import com.oass.util.DataAccessException;
import com.oass.util.Db;
import com.oass.util.HashUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Handles the 2-step forgot-password flow:
 *   GET  /forgot-password          → show step-1 email form
 *   POST /forgot-password          → generate & log OTP, show step-2 OTP form
 *   POST /forgot-password?action=reset → verify OTP, update password, redirect to login
 */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends BaseServlet {

    private static final Logger LOG = Logger.getLogger(ForgotPasswordServlet.class.getName());
    private static final int OTP_EXPIRY_MINUTES = 10;

    /** Shows the forgot-password page (step 1). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        render(req, resp, null, "auth/forgot-password");
    }

    /** Routes POST to either the OTP-send step or the password-reset step. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("reset".equals(action)) {
            handleReset(req, resp);
        } else {
            handleSendOtp(req, resp);
        }
    }

    /**
     * Step 1 POST: looks up user by email, generates a 6-digit OTP,
     * inserts it into password_resets, logs it, then forwards to step 2.
     */
    private void handleSendOtp(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = trim(req.getParameter("email"));
        if (email == null || !email.contains("@")) {
            flash(req, "error", "Please enter a valid email address.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        // Look up the user (do not reveal whether the email exists)
        Integer userId = Db.queryOne(
            "SELECT id FROM users WHERE email = ?",
            rs -> rs.getInt("id"),
            email.toLowerCase()
        );

        if (userId != null) {
            // Generate a 6-digit OTP and insert it
            String otp = String.format("%06d", new Random().nextInt(1_000_000));
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

            Db.insert(
                "INSERT INTO password_resets (user_id, otp, expires_at, used) VALUES (?, ?, ?, 0)",
                userId, otp, expiresAt
            );

            // Log the OTP to the server log (no real email sender needed)
            LOG.info("Password reset OTP for " + email + ": " + otp + " (expires " + expiresAt + ")");
        }
        // Always proceed to step 2 to avoid user enumeration
        req.setAttribute("step", "otp");
        req.setAttribute("email", email.toLowerCase());
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    /**
     * Step 2 POST: verifies the OTP, updates the user's password, marks OTP used,
     * then redirects to the login page with a success flash.
     */
    private void handleReset(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email       = trim(req.getParameter("email"));
        String otp         = trim(req.getParameter("otp"));
        String newPassword = req.getParameter("newPassword");
        String confirm     = req.getParameter("confirmPassword");

        // Basic input checks
        if (email == null || otp == null || newPassword == null) {
            flash(req, "error", "All fields are required.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }
        if (!newPassword.equals(confirm)) {
            req.setAttribute("step", "otp");
            req.setAttribute("email", email);
            req.setAttribute("error", "Passwords do not match.");
            flash(req, "error", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
            return;
        }

        // Look up user ID by email
        Integer userId = Db.queryOne(
            "SELECT id FROM users WHERE email = ?",
            rs -> rs.getInt("id"),
            email.toLowerCase()
        );

        if (userId == null) {
            flash(req, "error", "Invalid or expired OTP. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        // Find a valid, unused, non-expired OTP for this user
        Integer resetId = Db.queryOne(
            "SELECT id FROM password_resets " +
            "WHERE user_id = ? AND otp = ? AND used = 0 AND expires_at > NOW() " +
            "ORDER BY id DESC LIMIT 1",
            rs -> rs.getInt("id"),
            userId, otp
        );

        if (resetId == null) {
            flash(req, "error", "Invalid or expired OTP. Please request a new one.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        // Update the user's password
        String salt    = HashUtil.generateSalt();
        String newHash = HashUtil.hash(salt, newPassword);
        Db.update("UPDATE users SET password_hash = ?, salt = ? WHERE id = ?", newHash, salt, userId);

        // Mark the OTP as used
        Db.update("UPDATE password_resets SET used = 1 WHERE id = ?", resetId);

        LOG.info("Password reset successful for user id=" + userId);
        flash(req, "success", "Password reset successful! Please login with your new password.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /** Trims a string parameter, returning null if the parameter itself is null. */
    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
