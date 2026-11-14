package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.EvaluationDao;
import com.oass.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** GET /student/result/view?evalId=N : the student's own published result with the rubric breakdown. */
@WebServlet("/student/result/view")
public class StudentResultViewServlet extends BaseServlet {

    private final EvaluationDao evaluationDao = new EvaluationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        Integer evalId = idParam(req, "evalId");
        Map<String, Object> result = evalId == null ? null : evaluationDao.findPublishedResult(evalId, user.getId());
        if (result == null) {
            notFound(resp);            // unknown id, not published yet, or someone else's result
            return;
        }
        req.setAttribute("result", result);
        req.setAttribute("scores", evaluationDao.listScoreDetails(evalId));
        render(req, resp, "results", "student/result-view");
    }
}
