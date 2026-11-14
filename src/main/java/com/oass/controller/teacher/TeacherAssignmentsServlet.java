package com.oass.controller.teacher;

import com.oass.controller.BaseServlet;
import com.oass.dao.AcademicDao;
import com.oass.dao.AssignmentDao;
import com.oass.model.Assignment;
import com.oass.model.AssignmentStatus;
import com.oass.model.User;
import com.oass.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GET  /teacher/assignments         list of my assignments
 * GET  /teacher/assignment/create   empty create form (also /teacher/assignment/new)
 * POST /teacher/assignment/create   validate the form, save assignment + rubric in one transaction
 * Syllabus: Section D (Servlet, MVC).
 */
@WebServlet(urlPatterns = {"/teacher/assignments", "/teacher/assignment/create", "/teacher/assignment/new"})
public class TeacherAssignmentsServlet extends BaseServlet {

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final AcademicDao academicDao = new AcademicDao();

    /** Default rubric shown on a fresh form. */
    private static final String[] DEFAULT_NAMES = {"Content & Correctness", "Code Quality", "Documentation"};
    private static final String[] DEFAULT_MARKS = {"50", "30", "20"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        if (req.getServletPath().equals("/teacher/assignments")) {
            req.setAttribute("assignments", assignmentDao.listByTeacherWithCounts(user.getId()));
            render(req, resp, "assignments", "teacher/assignments");
        } else {
            showForm(req, resp, user);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            List<String> names = new ArrayList<>();
            List<BigDecimal> marks = new ArrayList<>();
            Assignment a = readForm(req, user, names, marks);

            assignmentDao.saveWithRubric(a, names, marks);

            flash(req, "success", a.getStatus() == AssignmentStatus.PUBLISHED
                    ? "Assignment published. Students of the section can see it now."
                    : "Assignment saved as draft.");
            resp.sendRedirect(req.getContextPath() + "/teacher/assignments");
        } catch (BusinessException e) {
            flash(req, "error", e.getMessage());       // show the form again; the JSP refills it from the request
            showForm(req, resp, user);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        req.setAttribute("classes", academicDao.listTeachingForTeacher(user.getId()));

        String[] names = req.getParameterValues("criterionName");
        String[] marks = req.getParameterValues("criterionMarks");
        boolean retry = names != null && marks != null && names.length == marks.length;
        req.setAttribute("critNames", retry ? names : DEFAULT_NAMES);
        req.setAttribute("critMarks", retry ? marks : DEFAULT_MARKS);

        render(req, resp, "assignments", "teacher/assignment-form");
    }

    /**
     * Reads and validates the whole form.
     * Fills criterionNames/criterionMarks and returns the Assignment (max marks = sum of the rubric).
     *
     * @throws BusinessException with a message for the teacher when something is wrong
     */
    private Assignment readForm(HttpServletRequest req, User user,
                                List<String> criterionNames, List<BigDecimal> criterionMarks) {
        Assignment a = new Assignment();
        a.setTeacherId(user.getId());

        String title = text(req, "title");
        if (title.isEmpty()) throw new BusinessException("Title is required.");
        if (title.length() > 150) throw new BusinessException("Title can be at most 150 characters.");
        a.setTitle(title);
        a.setDescription(text(req, "description"));

        // class = one subject + section that this teacher really teaches
        Integer teachingId = idParam(req, "teaching");
        Map<String, Object> teaching = teachingId == null ? null
                : academicDao.findTeaching(user.getId(), teachingId);
        if (teaching == null) throw new BusinessException("Please choose a subject and section you teach.");
        a.setSubjectId((int) teaching.get("subjectId"));
        a.setSectionId((int) teaching.get("sectionId"));

        // deadline, late policy
        a.setDeadline(dateTime(req, "deadline", true));
        if (a.getDeadline().isBefore(LocalDateTime.now())) throw new BusinessException("The deadline must be in the future.");

        a.setAllowLate(req.getParameter("allowLate") != null);
        a.setLatePenaltyPctPerDay(decimal(req, "penalty", "Late penalty", 0, 100, "10"));
        a.setMaxLatePenaltyPct(decimal(req, "maxPenalty", "Maximum late penalty", 0, 100, "50"));
        if (a.isAllowLate()) {
            LocalDateTime cutoff = dateTime(req, "cutoff", false);
            if (cutoff != null && cutoff.isBefore(a.getDeadline())) {
                throw new BusinessException("The hard cutoff cannot be before the deadline.");
            }
            a.setHardCutoff(cutoff);
        }

        // upload rules
        a.setAllowedTypes(normalizeTypes(text(req, "fileTypes")));
        a.setMaxFileMb(decimal(req, "maxSize", "Maximum file size", 1, 20, "10").intValue());

        // plagiarism + grading
        a.setPlagiarismEnabled(req.getParameter("plagiarism") != null);
        a.setPlagiarismThreshold(decimal(req, "threshold", "Plagiarism threshold", 1, 100, "70"));
        a.setBlindGrading(req.getParameter("blindGrading") != null);

        // rubric: max marks of the assignment = sum of the criteria
        String[] names = req.getParameterValues("criterionName");
        String[] marks = req.getParameterValues("criterionMarks");
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; names != null && marks != null && i < names.length && i < marks.length; i++) {
            String name = names[i].trim();
            String mark = marks[i].trim();
            if (name.isEmpty() && mark.isEmpty()) continue;                    // empty row: ignore
            if (name.isEmpty()) throw new BusinessException("Every rubric criterion needs a name.");
            BigDecimal value = parseDecimal(mark, "Marks for '" + name + "'");
            if (value.signum() <= 0) throw new BusinessException("Marks for '" + name + "' must be more than 0.");
            criterionNames.add(name.length() > 100 ? name.substring(0, 100) : name);
            criterionMarks.add(value);
            sum = sum.add(value);
        }
        if (criterionNames.isEmpty()) throw new BusinessException("Add at least one rubric criterion.");
        a.setMaxMarks(sum);

        a.setStatus("draft".equals(req.getParameter("action")) ? AssignmentStatus.DRAFT : AssignmentStatus.PUBLISHED);
        return a;
    }

    // ---------------------------------------------------------------- small form helpers

    private static String text(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /** Parses a datetime-local value. Returns null when it is optional and empty. */
    private static LocalDateTime dateTime(HttpServletRequest req, String name, boolean required) {
        String v = text(req, name);
        if (v.isEmpty()) {
            if (required) throw new BusinessException("Deadline is required.");
            return null;
        }
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Please enter a valid date and time.");
        }
    }

    /** Reads a number between min and max; an empty field uses the default. */
    private static BigDecimal decimal(HttpServletRequest req, String name, String label,
                                      int min, int max, String defaultValue) {
        String v = text(req, name);
        BigDecimal value = parseDecimal(v.isEmpty() ? defaultValue : v, label);
        if (value.compareTo(BigDecimal.valueOf(min)) < 0 || value.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new BusinessException(label + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    private static BigDecimal parseDecimal(String v, String label) {
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + " must be a number.");
        }
    }

    /** ".PDF, .docx zip" -> "pdf,docx,zip". Falls back to pdf,docx,txt when empty. */
    static String normalizeTypes(String raw) {
        List<String> types = new ArrayList<>();
        for (String part : raw.toLowerCase().split("[,\\s]+")) {
            String ext = part.replaceFirst("^\\.+", "");
            if (ext.matches("[a-z0-9]{1,10}") && !types.contains(ext)) types.add(ext);
        }
        return types.isEmpty() ? "pdf,docx,txt" : String.join(",", types);
    }
}
