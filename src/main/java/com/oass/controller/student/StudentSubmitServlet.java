package com.oass.controller.student;

import com.oass.controller.BaseServlet;
import com.oass.dao.AssignmentDao;
import com.oass.dao.SubmissionDao;
import com.oass.dao.UserDao;
import com.oass.model.Assignment;
import com.oass.model.AssignmentStatus;
import com.oass.model.Student;
import com.oass.model.User;
import com.oass.service.SubmissionService;
import com.oass.util.FileStorage;
import com.oass.util.HashUtil;
import com.oass.util.TextExtractor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * GET  /student/assignment/submit?id=N : upload form
 * POST /student/assignment/submit      : validate, save file on disk, extract text, store a new version
 *
 * Both GET and POST run the same checks (assignment published, student's section, deadline/cutoff).
 * Syllabus: Section D (Servlet, file upload with @MultipartConfig).
 */
@WebServlet("/student/assignment/submit")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 20L * 1024 * 1024, maxRequestSize = 21L * 1024 * 1024)
public class StudentSubmitServlet extends BaseServlet {

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final SubmissionDao submissionDao = new SubmissionDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Assignment a = loadSubmittable(req, resp);
        if (a == null) return;                          // an error or redirect was already sent

        req.setAttribute("assignment", a);
        req.setAttribute("allowedTypes", a.getAllowedTypes().replace(",", ", "));
        render(req, resp, "assignments", "student/assignment-submit");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Assignment a = loadSubmittable(req, resp);
        if (a == null) return;
        User user = currentUser(req);
        String backUrl = req.getContextPath() + "/student/assignment/submit?id=" + a.getId();

        // ---- 1. read and check the uploaded file
        Part file;
        try {
            file = req.getPart("file");
        } catch (IllegalStateException e) {            // thrown when the upload is bigger than @MultipartConfig allows
            flash(req, "error", "The file is too large (absolute limit 20 MB).");
            resp.sendRedirect(backUrl);
            return;
        }
        if (file == null || file.getSize() == 0) {
            flash(req, "error", "Please choose a file to upload.");
            resp.sendRedirect(backUrl);
            return;
        }

        String originalName = FileStorage.cleanName(file.getSubmittedFileName());
        String ext = FileStorage.extensionOf(originalName);
        List<String> allowed = Arrays.asList(a.getAllowedTypes().split(","));
        if (!allowed.contains(ext)) {
            flash(req, "error", "File type ." + ext + " is not allowed. Allowed: " + String.join(", ", allowed) + ".");
            resp.sendRedirect(backUrl);
            return;
        }
        long maxBytes = a.getMaxFileMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            flash(req, "error", "The file is larger than " + a.getMaxFileMb() + " MB.");
            resp.sendRedirect(backUrl);
            return;
        }

        // ---- 2. save on disk (UUID name), hash it, extract its text
        byte[] data;
        try (var in = file.getInputStream()) {
            data = in.readAllBytes();
        }
        String sha256 = HashUtil.sha256Hex(data);
        String text = TextExtractor.extract(data, ext);
        String storedName = FileStorage.save(data, ext);

        // ---- 3. store the new version in the database
        String comment = req.getParameter("comment");
        if (comment != null && comment.length() > 500) comment = comment.substring(0, 500);
        boolean late = LocalDateTime.now().isAfter(a.getDeadline());

        try {
            var saved = submissionDao.addSubmission(new SubmissionDao.NewSubmission(
                    a.getId(), user.getId(), originalName, storedName, data.length, sha256, comment, late, text));
            flash(req, "success", "Submitted! Version " + saved.versionNo() + ", receipt " + saved.receiptNo()
                    + (late ? " (marked late)." : "."));
        } catch (RuntimeException e) {
            FileStorage.delete(storedName);            // do not leave an unused file on disk
            throw e;
        }
        resp.sendRedirect(req.getContextPath() + "/student/submissions");
    }

    /**
     * Loads the assignment and checks that this student may submit to it right now.
     * Returns null after sending a 404 or a redirect with an error message.
     */
    private Assignment loadSubmittable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        Integer id = idParam(req, "id");
        Assignment a = id == null ? null : assignmentDao.findById(id);
        Student student = userDao.findStudentById(user.getId());

        if (a == null || a.getStatus() == AssignmentStatus.DRAFT
                || student == null || student.getSectionId() != a.getSectionId()) {
            notFound(resp);                            // unknown, draft, or another section's assignment
            return null;
        }

        String problem = null;
        if (a.getStatus() == AssignmentStatus.CLOSED) {
            problem = "This assignment is closed.";
        } else {
            problem = SubmissionService.checkWindow(LocalDateTime.now(), a.getDeadline(),
                    a.isAllowLate(), a.getHardCutoff());
        }
        if (problem != null) {
            flash(req, "error", problem);
            resp.sendRedirect(req.getContextPath() + "/student/assignments");
            return null;
        }
        return a;
    }
}
