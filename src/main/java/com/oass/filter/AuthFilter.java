package com.oass.filter;

import com.oass.model.Role;
import com.oass.model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Enforces authentication and role-based access for every request.
 * Public paths: /login, /logout, /register/*, /forgot-password, /assets/*, error pages.
 * Role prefixes: /student/* requires STUDENT, /teacher/* requires TEACHER.
 * Syllabus: Section D (Filter, session management, security).
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();

        // Always allow static assets and public auth pages
        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check session
        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            // Not logged in → redirect to login
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Role check based on path prefix
        if (path.startsWith("/student/") && user.getRole() != Role.STUDENT) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (path.startsWith("/teacher/") && user.getRole() != Role.TEACHER) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    /** Returns true for paths that any visitor (logged in or not) may access. */
    private boolean isPublic(String path) {
        return path.equals("/login")
            || path.equals("/logout")
            || path.startsWith("/register/")
            || path.equals("/forgot-password")
            || path.startsWith("/assets/")
            || path.startsWith("/error");
    }
}
