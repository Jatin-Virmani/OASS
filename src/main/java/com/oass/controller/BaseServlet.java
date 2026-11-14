package com.oass.controller;

import com.oass.dao.UserDao;
import com.oass.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Abstract base class for all OASS servlets.
 * Provides currentUser(), render(), flash(), notFound() and forbidden() helpers.
 * Syllabus: Section D (Servlet, abstract class, MVC).
 */
public abstract class BaseServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    /** Returns the logged-in User from the session, or null. */
    protected User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session == null) ? null : (User) session.getAttribute("user");
    }

    /**
     * Forwards to /WEB-INF/views/{view}.jsp after setting the active nav and
     * the flash message (once). Also refreshes the unread notification count.
     */
    protected void render(HttpServletRequest req, HttpServletResponse resp,
                          String activeNav, String view)
            throws ServletException, IOException {

        req.setAttribute("activeNav", activeNav);

        // Refresh unread count in the session so the bell stays accurate
        User user = currentUser(req);
        if (user != null) {
            int unread = userDao.countUnread(user.getId());
            req.getSession().setAttribute("unreadCount", unread);
        }

        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp")
           .forward(req, resp);
    }

    /** Stores a success or error flash message in the session (shown once by layout.tag). */
    protected void flash(HttpServletRequest req, String type, String message) {
        String key = "success".equals(type) ? "flashSuccess" : "flashError";
        req.getSession().setAttribute(key, message);
    }

    /** Sends a 404 response using the custom error page. */
    protected void notFound(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /** Sends a 403 response using the custom error page. */
    protected void forbidden(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    /** Reads a whole-number request parameter (an id). Returns null if it is missing or not a number. */
    protected Integer idParam(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null) return null;
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
