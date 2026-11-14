package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.AssignmentDao;
import com.oass.dao.SubmissionDao;
import com.oass.dao.UserDao;
import com.oass.model.Assignment;
import com.oass.model.Student;
import com.oass.model.SubmissionState;
import com.oass.model.User;
import com.oass.service.SubmissionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/student/assignments")
public class StudentAssignmentsServlet extends BaseServlet {
    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final SubmissionDao submissionDao = new SubmissionDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }
        
        Student student = userDao.findStudentById(user.getId());
        int sectionId = student != null ? student.getSectionId() : 1;
        List<Assignment> assignments = assignmentDao.listVisibleForSection(sectionId);
        
        LocalDateTime now = LocalDateTime.now();
        for (Assignment a : assignments) {
            LocalDateTime subAt = submissionDao.currentSubmittedAt(user.getId(), a.getId());
            SubmissionState state = SubmissionService.stateFor(now, a.getDeadline(), subAt);
            a.setStatus(com.oass.model.AssignmentStatus.valueOf(state.name().equals("MISSED") ? "CLOSED" : a.getStatus().name()));
        }
        
        req.setAttribute("assignments", assignments);
        render(req, resp, "assignments", "student/assignments");
    }
}
