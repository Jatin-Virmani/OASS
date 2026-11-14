package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.dao.AcademicDao;
import com.oass.dao.QuestionDao;
import com.oass.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** GET/POST /teacher/questions : the teacher's own question bank (each question belongs to a subject they teach). */
@WebServlet("/teacher/questions")
public class TeacherQuestionsServlet extends BaseServlet {

    private static final Set<String> TYPES = Set.of("SHORT", "DESCRIPTIVE", "ESSAY");
    private static final Set<String> LEVELS = Set.of("EASY", "MEDIUM", "HARD");

    private final QuestionDao questionDao = new QuestionDao();
    private final AcademicDao academicDao = new AcademicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("subjects", academicDao.listSubjectsForTeacher(user.getId()));
        req.setAttribute("questions", questionDao.listForTeacher(user.getId()));
        render(req, resp, "questions", "teacher/questions");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String error = null;

        Integer subjectId = idParam(req, "subject");
        List<Map<String, Object>> mySubjects = academicDao.listSubjectsForTeacher(user.getId());
        boolean teachesSubject = subjectId != null && mySubjects.stream().anyMatch(s -> s.get("id").equals(subjectId));

        String text = param(req, "questionText");
        String type = param(req, "type").toUpperCase();
        String level = param(req, "difficulty").toUpperCase();
        BigDecimal marks = null;
        try {
            marks = new BigDecimal(param(req, "marks"));
        } catch (NumberFormatException ignored) {
            // handled below
        }

        if (!teachesSubject)                      error = "Choose a subject you teach.";
        else if (text.isEmpty())                  error = "Question text is required.";
        else if (!TYPES.contains(type))           error = "Choose a valid question type.";
        else if (!LEVELS.contains(level))         error = "Choose a valid difficulty.";
        else if (marks == null || marks.signum() <= 0 || marks.compareTo(BigDecimal.valueOf(1000)) >= 0)
                                                  error = "Marks must be a number above 0.";

        if (error != null) {
            flash(req, "error", error);
        } else {
            String topic = param(req, "topic");
            questionDao.add(user.getId(), subjectId, type, text, marks, level,
                    topic.isEmpty() ? null : (topic.length() > 100 ? topic.substring(0, 100) : topic));
            flash(req, "success", "Question added to your question bank.");
        }
        resp.sendRedirect(req.getContextPath() + "/teacher/questions");
    }

    private static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
}
