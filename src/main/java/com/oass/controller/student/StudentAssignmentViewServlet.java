package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.AssignmentDao;
import com.oass.dao.EvaluationDao;
import com.oass.dao.UserDao;
import com.oass.model.Assignment;
import com.oass.model.AssignmentStatus;
import com.oass.model.Student;
import com.oass.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** GET /student/assignment/view?id=N : details and rubric of one assignment of the student's own section. */
@WebServlet("/student/assignment/view")
public class StudentAssignmentViewServlet extends BaseServlet {

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        Integer id = idParam(req, "id");
        Assignment a = id == null ? null : assignmentDao.findById(id);
        Student student = userDao.findStudentById(user.getId());

        // 404 for unknown ids, drafts, and assignments of another section
        if (a == null || a.getStatus() == AssignmentStatus.DRAFT
                || student == null || student.getSectionId() != a.getSectionId()) {
            notFound(resp);
            return;
        }

        req.setAttribute("assignment", a);
        req.setAttribute("criteria", evaluationDao.listCriteria(a.getId()));
        render(req, resp, "assignments", "student/assignment-view");
    }
}
