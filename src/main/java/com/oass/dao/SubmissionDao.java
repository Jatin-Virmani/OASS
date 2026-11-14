package com.oass.dao;

import com.oass.util.Db;
import com.oass.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data access for the submissions table and the current_submissions view.
 * P1 only needs the methods required for dashboard state counting.
 * Syllabus: Section C (JDBC, DAO pattern).
 */
public class SubmissionDao {

    /**
     * Returns the submission time of the current (latest accepted) submission
     * for the given student and assignment, or null if the student has not submitted.
     */
    public LocalDateTime currentSubmittedAt(int studentId, int assignmentId) {
        String sql = """
            SELECT submitted_at FROM current_submissions
            WHERE student_id = ? AND assignment_id = ?
            """;
        return Db.queryOne(sql,
                rs -> rs.getObject("submitted_at", LocalDateTime.class),
                studentId, assignmentId);
    }

    /**
     * Returns true if the current submission for the given student and assignment
     * is marked as late (is_late = 1).
     */
    public boolean isCurrentSubmissionLate(int studentId, int assignmentId) {
        String sql = """
            SELECT is_late FROM current_submissions
            WHERE student_id = ? AND assignment_id = ?
            """;
        Boolean late = Db.queryOne(sql, rs -> rs.getBoolean("is_late"), studentId, assignmentId);
        return Boolean.TRUE.equals(late);
    }

    /** Counts total submissions across the whole system. */
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM submissions WHERE upload_status = 'ACCEPTED' AND deleted_at IS NULL";
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1));
        return n == null ? 0 : n;
    }

    /** Counts accepted submissions for a specific assignment (for teacher dashboard). */
    public int countByAssignment(int assignmentId) {
        String sql = """
            SELECT COUNT(*) FROM current_submissions WHERE assignment_id = ?
            """;
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1), assignmentId);
        return n == null ? 0 : n;
    }

    /**
     * Returns all current (latest accepted) submissions for a student across all assignments.
     * Joins assignment, subject, and evaluation tables for the display columns.
     */
    public List<Map<String,Object>> listForStudent(int studentId) {
        String sql = """
            SELECT s.*, a.title AS assignment_title, sub.code AS subject_code,
                   s.receipt_no, s.version_no, s.is_late, s.submitted_at,
                   e.final_marks, e.status AS eval_status, e.feedback
            FROM current_submissions s
            JOIN assignments a ON a.id = s.assignment_id
            JOIN subjects sub ON sub.id = a.subject_id
            LEFT JOIN evaluations e ON e.submission_id = s.id
            WHERE s.student_id = ?
            ORDER BY s.submitted_at DESC
            """;
        return Db.query(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("assignmentId", rs.getInt("assignment_id"));
            m.put("assignmentTitle", rs.getString("assignment_title"));
            m.put("subjectCode", rs.getString("subject_code"));
            m.put("receiptNo", rs.getString("receipt_no"));
            m.put("versionNo", rs.getInt("version_no"));
            m.put("isLate", rs.getBoolean("is_late"));
            m.put("submittedAt", TimeUtil.format(rs.getObject("submitted_at", java.time.LocalDateTime.class)));
            m.put("finalMarks", rs.getBigDecimal("final_marks"));
            m.put("evalStatus", rs.getString("eval_status"));
            m.put("feedback", rs.getString("feedback"));
            return m;
        }, studentId);
    }

    /**
     * Returns all PUBLISHED evaluations (results) for a student.
     * Joins evaluation, submission, assignment, and subject tables.
     */
    public List<Map<String,Object>> listResultsForStudent(int studentId) {
        String sql = """
            SELECT e.*, a.title AS assignment_title, a.max_marks,
                   sub.code AS subject_code, s.is_late, s.receipt_no
            FROM evaluations e
            JOIN submissions s ON s.id = e.submission_id
            JOIN assignments a ON a.id = s.assignment_id
            JOIN subjects sub ON sub.id = a.subject_id
            WHERE s.student_id = ? AND e.status = 'PUBLISHED'
            ORDER BY e.published_at DESC
            """;
        return Db.query(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("evalId", rs.getInt("id"));
            m.put("assignmentTitle", rs.getString("assignment_title"));
            m.put("subjectCode", rs.getString("subject_code"));
            m.put("maxMarks", rs.getBigDecimal("max_marks"));
            m.put("finalMarks", rs.getBigDecimal("final_marks"));
            m.put("feedback", rs.getString("feedback"));
            m.put("isLate", rs.getBoolean("is_late"));
            m.put("receiptNo", rs.getString("receipt_no"));
            m.put("publishedAt", TimeUtil.format(rs.getObject("published_at", java.time.LocalDateTime.class)));
            return m;
        }, studentId);
    }

    /** Everything needed to store one new upload. */
    public record NewSubmission(int assignmentId, int studentId, String originalName, String storedName,
                                long sizeBytes, String sha256, String comment, boolean late, String textContent) {}

    /** What the student gets back after a successful upload. */
    public record SavedSubmission(long id, int versionNo, String receiptNo) {}

    /**
     * Stores an upload as the next version for this student and assignment.
     * Version number and insert happen in one transaction (the rows are locked while the next number is read).
     */
    public SavedSubmission addSubmission(NewSubmission n) {
        return Db.transaction(tx -> {
            Integer latest = tx.queryOne("""
                SELECT MAX(version_no) FROM submissions
                WHERE assignment_id = ? AND student_id = ? FOR UPDATE
                """, rs -> rs.getInt(1), n.assignmentId(), n.studentId());
            int version = (latest == null ? 0 : latest) + 1;
            String receipt = "REC-" + n.assignmentId() + "-" + n.studentId() + "-V" + version + "-"
                    + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            long id = tx.insert("""
                INSERT INTO submissions (assignment_id, student_id, version_no, original_name, stored_name,
                    size_bytes, sha256, receipt_no, comment_text, submitted_at, is_late, text_content)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """, n.assignmentId(), n.studentId(), version, n.originalName(), n.storedName(),
                n.sizeBytes(), n.sha256(), receipt, n.comment(), LocalDateTime.now(), n.late(), n.textContent());
            return new SavedSubmission(id, version, receipt);
        });
    }
}
