package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.dao.PlagiarismDao;
import com.oass.model.User;
import com.oass.service.PlagiarismService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * GET  /teacher/plagiarism          list of compared pairs
 * GET  /teacher/plagiarism/compare  one pair side by side, matching phrases highlighted
 * POST /teacher/plagiarism          run the check on this teacher's assignments
 * Syllabus: Section D (Servlet, MVC).
 */
@WebServlet(urlPatterns = {"/teacher/plagiarism", "/teacher/plagiarism/compare"})
public class TeacherPlagiarismServlet extends BaseServlet {

    private final PlagiarismDao plagiarismDao = new PlagiarismDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);

        if (req.getServletPath().equals("/teacher/plagiarism/compare")) {
            showPair(req, resp, user);
        } else {
            req.setAttribute("similarityResults", plagiarismDao.listResults(user.getId()));
            render(req, resp, "plagiarism", "teacher/plagiarism");
        }
    }

    private void showPair(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        Integer pairId = idParam(req, "id");
        Map<String, Object> pair = pairId == null ? null : plagiarismDao.findPair(pairId, user.getId());
        if (pair == null) {
            notFound(resp);
            return;
        }
        String textA = (String) pair.get("textA");
        String textB = (String) pair.get("textB");
        pair.put("htmlA", PlagiarismService.highlight(textA, textB));   // already HTML-escaped
        pair.put("htmlB", PlagiarismService.highlight(textB, textA));
        req.setAttribute("pair", pair);
        render(req, resp, "plagiarism", "teacher/plagiarism-compare");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        int pairs = runCheck(user.getId());
        flash(req, "success", "Plagiarism check finished: " + pairs + " pair(s) compared.");
        resp.sendRedirect(req.getContextPath() + "/teacher/plagiarism");
    }

    /** Compares every two different students' current submissions of the same assignment. */
    private int runCheck(int teacherId) {
        List<Map<String, Object>> subs = plagiarismDao.listComparable(teacherId);
        int compared = 0;

        for (int i = 0; i < subs.size(); i++) {
            for (int j = i + 1; j < subs.size(); j++) {
                Map<String, Object> s1 = subs.get(i);
                Map<String, Object> s2 = subs.get(j);
                if (!s1.get("assignmentId").equals(s2.get("assignmentId"))) continue;
                if (s1.get("studentId").equals(s2.get("studentId"))) continue;

                int idA = Math.min((int) s1.get("id"), (int) s2.get("id"));   // table rule: submission_a < submission_b
                int idB = Math.max((int) s1.get("id"), (int) s2.get("id"));

                double jaccard = PlagiarismService.jaccard((String) s1.get("text"), (String) s2.get("text"));
                double cosine  = PlagiarismService.cosine((String) s1.get("text"), (String) s2.get("text"));
                double score   = PlagiarismService.score(jaccard, cosine);
                String risk    = PlagiarismService.riskFor(score, (double) s1.get("threshold"));

                plagiarismDao.saveResult((int) s1.get("assignmentId"), idA, idB,
                        percent(jaccard * 100), percent(cosine * 100), percent(score), risk);
                compared++;
            }
        }
        return compared;
    }

    private static BigDecimal percent(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
