package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.dao.AssignmentDao;
import com.oass.dao.EvaluationDao;
import com.oass.model.Assignment;
import com.oass.model.User;
import com.oass.service.EvaluationService;
import com.oass.util.FileStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything a teacher does with the submissions of an assignment:
 *   GET  /teacher/submissions?id=A           list of current submissions
 *   GET  /teacher/submission/evaluate?id=S   evaluation form (rubric loaded from the database)
 *   POST /teacher/submission/evaluate        save as draft or finalize (publish)
 *   GET  /teacher/submission/file?id=S       download the uploaded file
 *   GET  /teacher/marks/export?id=A          marks of the whole section as CSV
 * Every request checks that the assignment belongs to the logged-in teacher.
 */
@WebServlet(urlPatterns = {
    "/teacher/submissions", "/teacher/submission/evaluate",
    "/teacher/submission/file", "/teacher/marks/export"
})
public class TeacherSubmissionsServlet extends BaseServlet {

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/teacher/submissions"        -> showList(req, resp);
            case "/teacher/submission/evaluate" -> showEvaluateForm(req, resp);
            case "/teacher/submission/file"     -> sendFile(req, resp);
            case "/teacher/marks/export"        -> exportCsv(req, resp);
            default -> notFound(resp);
        }
    }

    // ------------------------------------------------------------------ list

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Assignment a = ownAssignment(req, resp);
        if (a == null) return;

        req.setAttribute("assignment", a);
        req.setAttribute("rows", evaluationDao.listSubmissionsForAssignment(a.getId()));
        render(req, resp, "assignments", "teacher/submissions");
    }

    // ------------------------------------------------------------------ evaluate

    private void showEvaluateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, Object> sub = ownSubmission(req, resp, "id");
        if (sub == null) return;
        int submissionId = (int) sub.get("id");

        Map<String, Object> evaluation = evaluationDao.findEvaluation(submissionId);
        req.setAttribute("sub", sub);
        req.setAttribute("criteria", evaluationDao.listCriteria((int) sub.get("assignmentId")));
        req.setAttribute("evaluation", evaluation);
        req.setAttribute("scores", evaluation == null
                ? Map.of() : evaluationDao.scoreMap((int) evaluation.get("id")));

        String text = (String) sub.get("textContent");
        req.setAttribute("preview", text == null ? null : text.length() > 4000 ? text.substring(0, 4000) + " …" : text);
        render(req, resp, "assignments", "teacher/evaluate");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        Map<String, Object> sub = ownSubmission(req, resp, "submissionId");
        if (sub == null) return;
        int submissionId = (int) sub.get("id");
        String backUrl = req.getContextPath() + "/teacher/submission/evaluate?id=" + submissionId;
        boolean finalizeNow = "finalize".equals(req.getParameter("action"));

        // read one score per rubric criterion and check 0 <= score <= criterion max
        Map<Integer, BigDecimal> scores = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> c : evaluationDao.listCriteria((int) sub.get("assignmentId"))) {
            int criterionId = (int) c.get("id");
            BigDecimal max = (BigDecimal) c.get("maxMarks");
            String raw = req.getParameter("score_" + criterionId);

            BigDecimal score;
            if (raw == null || raw.isBlank()) {
                if (finalizeNow) { flash(req, "error", "Enter marks for '" + c.get("name") + "' before finalizing."); resp.sendRedirect(backUrl); return; }
                score = BigDecimal.ZERO;                     // a draft may be incomplete
            } else {
                try {
                    score = new BigDecimal(raw.trim());
                } catch (NumberFormatException e) {
                    flash(req, "error", "Marks for '" + c.get("name") + "' must be a number.");
                    resp.sendRedirect(backUrl);
                    return;
                }
            }
            if (score.signum() < 0 || score.compareTo(max) > 0) {
                flash(req, "error", "Marks for '" + c.get("name") + "' must be between 0 and " + max + ".");
                resp.sendRedirect(backUrl);
                return;
            }
            scores.put(criterionId, score);
            total = total.add(score);
        }

        // late penalty is calculated automatically from the assignment's late policy
        BigDecimal penalty = EvaluationService.latePenalty(total,
                (LocalDateTime) sub.get("submittedAtRaw"), (LocalDateTime) sub.get("deadlineRaw"),
                (BigDecimal) sub.get("latePenaltyPctPerDay"), (BigDecimal) sub.get("maxLatePenaltyPct"));
        BigDecimal finalMarks = EvaluationService.finalMarks(total, penalty);

        String feedback = req.getParameter("feedback") == null ? "" : req.getParameter("feedback").trim();
        evaluationDao.saveEvaluation(submissionId, scores, total, penalty, finalMarks, feedback,
                finalizeNow ? "PUBLISHED" : "DRAFT", user.getId());

        if (finalizeNow) {
            flash(req, "success", "Marks published: " + finalMarks + " (late penalty " + penalty + ").");
            resp.sendRedirect(req.getContextPath() + "/teacher/submissions?id=" + sub.get("assignmentId"));
        } else {
            flash(req, "success", "Draft saved. The student cannot see it yet.");
            resp.sendRedirect(backUrl);
        }
    }

    // ------------------------------------------------------------------ file download

    private void sendFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> sub = ownSubmission(req, resp, "id");
        if (sub == null) return;
        String storedName = (String) sub.get("storedName");
        if (storedName == null) { notFound(resp); return; }          // seeded demo data has no file on disk

        Path file = FileStorage.find(storedName);
        if (!Files.exists(file)) { notFound(resp); return; }

        String safeName = String.valueOf(sub.get("originalName")).replaceAll("[^A-Za-z0-9._-]", "_");
        resp.setContentType("application/octet-stream");             // never let the browser run an uploaded file
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + safeName + "\"");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }

    // ------------------------------------------------------------------ CSV export

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Assignment a = ownAssignment(req, resp);
        if (a == null) return;

        resp.setContentType("text/csv");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Content-Disposition", "attachment; filename=\"marks-assignment-" + a.getId() + ".csv\"");

        PrintWriter out = resp.getWriter();
        out.print('\uFEFF');                                          // byte-order mark so Excel reads UTF-8 correctly
        out.print("Roll No,Name,Status,Submitted At,Late,Late Penalty,Final Marks,Max Marks\r\n");
        for (Map<String, Object> r : evaluationDao.listMarksForAssignment(a.getId())) {
            LocalDateTime submittedAt = (LocalDateTime) r.get("submittedAt");
            String evalStatus = (String) r.get("evalStatus");
            String status = submittedAt == null ? "Not submitted"
                          : "PUBLISHED".equals(evalStatus) ? "Graded"
                          : "DRAFT".equals(evalStatus) ? "Draft (not published)" : "Submitted";
            boolean graded = "PUBLISHED".equals(evalStatus);

            out.print(String.join(",",
                csv(r.get("rollNo")), csv(r.get("name")), csv(status),
                csv(submittedAt == null ? "" : submittedAt.toString().replace('T', ' ')),
                csv(submittedAt == null ? "" : (boolean) r.get("isLate") ? "Yes" : "No"),
                csv(graded ? r.get("latePenalty") : ""), csv(graded ? r.get("finalMarks") : ""),
                csv(a.getMaxMarks())) + "\r\n");
        }
    }

    /** One CSV cell: quoted, quotes doubled, and a leading = + - @ neutralised so Excel cannot run it as a formula. */
    private static String csv(Object value) {
        String s = value == null ? "" : value.toString();
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    // ------------------------------------------------------------------ ownership checks

    /** The assignment named by ?id=, if it belongs to the logged-in teacher. Otherwise sends 404/403 and returns null. */
    private Assignment ownAssignment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = idParam(req, "id");
        Assignment a = id == null ? null : assignmentDao.findById(id);
        if (a == null) { notFound(resp); return null; }
        if (a.getTeacherId() != currentUser(req).getId()) { forbidden(resp); return null; }
        return a;
    }

    /** The submission named by the given parameter, if its assignment belongs to the logged-in teacher. */
    private Map<String, Object> ownSubmission(HttpServletRequest req, HttpServletResponse resp, String param)
            throws IOException {
        Integer id = idParam(req, param);
        Map<String, Object> sub = id == null ? null : evaluationDao.findSubmission(id);
        if (sub == null) { notFound(resp); return null; }
        if ((int) sub.get("teacherId") != currentUser(req).getId()) { forbidden(resp); return null; }
        return sub;
    }
}
