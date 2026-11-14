package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.SubmissionDao;
import com.oass.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/student/submissions")
public class StudentSubmissionsServlet extends BaseServlet {
    private final SubmissionDao submissionDao = new SubmissionDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }
        req.setAttribute("submissions", submissionDao.listForStudent(user.getId()));
        render(req, resp, "submissions", "student/submissions");
    }
}
