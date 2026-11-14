package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.dao.AcademicDao;
import com.oass.dao.AnnouncementDao;
import com.oass.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** GET/POST /teacher/announcements : post a notice to one of the sections the teacher teaches. */
@WebServlet("/teacher/announcements")
public class TeacherAnnouncementsServlet extends BaseServlet {

    private final AnnouncementDao announcementDao = new AnnouncementDao();
    private final AcademicDao academicDao = new AcademicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("sections", academicDao.listSectionsForTeacher(user.getId()));
        req.setAttribute("announcements", announcementDao.listForTeacher(user.getId()));
        render(req, resp, "announcements", "teacher/announcements");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        Integer sectionId = idParam(req, "section");
        String title = req.getParameter("title") == null ? "" : req.getParameter("title").trim();
        String body = req.getParameter("body") == null ? "" : req.getParameter("body").trim();

        boolean teachesSection = sectionId != null && academicDao.listSectionsForTeacher(user.getId())
                .stream().anyMatch(s -> s.get("id").equals(sectionId));

        if (!teachesSection) {
            flash(req, "error", "Choose a section you teach.");
        } else if (title.isEmpty() || title.length() > 150 || body.isEmpty()) {
            flash(req, "error", "A title (up to 150 characters) and a message are required.");
        } else {
            announcementDao.add(user.getId(), sectionId, title, body);
            flash(req, "success", "Announcement posted to the section.");
        }
        resp.sendRedirect(req.getContextPath() + "/teacher/announcements");
    }
}
