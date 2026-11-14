package com.oass.dao;

import com.oass.util.Db;
import com.oass.util.TimeUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Data access for evaluations, rubric criteria and evaluation_scores. */
public class EvaluationDao {

    /** Returns all submissions for an assignment with student name and eval status. */
    public List<Map<String,Object>> listSubmissionsForAssignment(int assignmentId) {
        String sql = """
            SELECT cs.*, u.name AS student_name, st.roll_no,
                   e.id AS eval_id, e.final_marks, e.status AS eval_status
            FROM current_submissions cs
            JOIN students st ON st.user_id = cs.student_id
            JOIN users u ON u.id = st.user_id
            LEFT JOIN evaluations e ON e.submission_id = cs.id
            WHERE cs.assignment_id = ?
            ORDER BY u.name
            """;
        return Db.query(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("studentName", rs.getString("student_name"));
            m.put("rollNo", rs.getString("roll_no"));
            m.put("receiptNo", rs.getString("receipt_no"));
            m.put("isLate", rs.getBoolean("is_late"));
            m.put("submittedAt", TimeUtil.format(rs.getObject("submitted_at", LocalDateTime.class)));
            m.put("evalId", rs.getObject("eval_id"));
            m.put("finalMarks", rs.getBigDecimal("final_marks"));
            m.put("evalStatus", rs.getString("eval_status"));
            return m;
        }, assignmentId);
    }

    /** Returns rubric criteria for an assignment. */
    public List<Map<String,Object>> listCriteria(int assignmentId) {
        return Db.query("SELECT * FROM rubric_criteria WHERE assignment_id = ? ORDER BY sort_order",
            rs -> {
                Map<String,Object> m = new java.util.LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("name", rs.getString("name"));
                m.put("maxMarks", rs.getBigDecimal("max_marks"));
                return m;
            }, assignmentId);
    }

    /** Returns one submission with its assignment details, or null. */
    public Map<String,Object> findSubmission(int submissionId) {
        String sql = """
            SELECT s.*, u.name AS student_name, st.roll_no,
                   a.title AS assignment_title, a.max_marks AS assignment_max,
                   a.teacher_id, a.blind_grading, a.deadline AS assignment_deadline,
                   a.late_penalty_pct_per_day, a.max_late_penalty_pct
            FROM submissions s
            JOIN students st ON st.user_id = s.student_id
            JOIN users u ON u.id = st.user_id
            JOIN assignments a ON a.id = s.assignment_id
            WHERE s.id = ?
            """;
        return Db.queryOne(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("teacherId", rs.getInt("teacher_id"));
            m.put("blindGrading", rs.getBoolean("blind_grading"));
            m.put("studentName", rs.getString("student_name"));
            m.put("rollNo", rs.getString("roll_no"));
            m.put("assignmentId", rs.getInt("assignment_id"));
            m.put("assignmentTitle", rs.getString("assignment_title"));
            m.put("assignmentMax", rs.getBigDecimal("assignment_max"));
            m.put("receiptNo", rs.getString("receipt_no"));
            m.put("isLate", rs.getBoolean("is_late"));
            m.put("textContent", rs.getString("text_content"));
            m.put("originalName", rs.getString("original_name"));
            m.put("storedName", rs.getString("stored_name"));
            m.put("sha256", rs.getString("sha256"));
            m.put("submittedAt", TimeUtil.format(rs.getObject("submitted_at", LocalDateTime.class)));
            m.put("submittedAtRaw", rs.getObject("submitted_at", LocalDateTime.class));
            m.put("deadlineRaw", rs.getObject("assignment_deadline", LocalDateTime.class));
            m.put("latePenaltyPctPerDay", rs.getBigDecimal("late_penalty_pct_per_day"));
            m.put("maxLatePenaltyPct", rs.getBigDecimal("max_late_penalty_pct"));
            return m;
        }, submissionId);
    }

    /** Returns existing evaluation for a submission, or null. */
    public Map<String,Object> findEvaluation(int submissionId) {
        String sql = "SELECT * FROM evaluations WHERE submission_id = ?";
        return Db.queryOne(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("total", rs.getBigDecimal("total"));
            m.put("latePenalty", rs.getBigDecimal("late_penalty"));
            m.put("finalMarks", rs.getBigDecimal("final_marks"));
            m.put("feedback", rs.getString("feedback"));
            m.put("status", rs.getString("status"));
            return m;
        }, submissionId);
    }

    /** Marks per criterion of one evaluation: criterion id -> marks. */
    public Map<Integer, java.math.BigDecimal> scoreMap(long evalId) {
        Map<Integer, java.math.BigDecimal> scores = new java.util.LinkedHashMap<>();
        Db.query("SELECT criterion_id, marks FROM evaluation_scores WHERE evaluation_id = ?", rs -> {
            scores.put(rs.getInt("criterion_id"), rs.getBigDecimal("marks"));
            return null;
        }, evalId);
        return scores;
    }

    /**
     * Saves (insert or update) an evaluation and all its rubric scores in ONE transaction.
     * status: "DRAFT" (teacher only) or "PUBLISHED" (student can see it; published_at is set).
     *
     * @return the evaluation id
     */
    public long saveEvaluation(int submissionId, Map<Integer, java.math.BigDecimal> scores,
                               java.math.BigDecimal total, java.math.BigDecimal latePenalty,
                               java.math.BigDecimal finalMarks, String feedback,
                               String status, int evaluatedBy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishedAt = "PUBLISHED".equals(status) ? now : null;

        return Db.transaction(tx -> {
            Integer existingId = tx.queryOne("SELECT id FROM evaluations WHERE submission_id = ?",
                    rs -> rs.getInt("id"), submissionId);
            long evalId;
            if (existingId != null) {
                evalId = existingId;
                tx.update("""
                    UPDATE evaluations SET total=?, late_penalty=?, final_marks=?, feedback=?, status=?,
                           evaluated_by=?, evaluated_at=?, published_at=? WHERE id=?
                    """, total, latePenalty, finalMarks, feedback, status, evaluatedBy, now, publishedAt, evalId);
            } else {
                evalId = tx.insert("""
                    INSERT INTO evaluations (submission_id, total, late_penalty, final_marks, feedback, status,
                                             evaluated_by, evaluated_at, published_at)
                    VALUES (?,?,?,?,?,?,?,?,?)
                    """, submissionId, total, latePenalty, finalMarks, feedback, status, evaluatedBy, now, publishedAt);
            }
            for (Map.Entry<Integer, java.math.BigDecimal> e : scores.entrySet()) {
                tx.update("""
                    INSERT INTO evaluation_scores (evaluation_id, criterion_id, marks) VALUES (?,?,?)
                    ON DUPLICATE KEY UPDATE marks = VALUES(marks)
                    """, evalId, e.getKey(), e.getValue());
            }
            return evalId;
        });
    }

    /** A student's own PUBLISHED result by evaluation id, or null (also null if it is someone else's). */
    public Map<String,Object> findPublishedResult(int evalId, int studentId) {
        String sql = """
            SELECT e.*, a.title AS assignment_title, a.max_marks, sub.code AS subject_code,
                   s.receipt_no, s.is_late, s.version_no, s.submitted_at
            FROM evaluations e
            JOIN submissions s ON s.id = e.submission_id
            JOIN assignments a ON a.id = s.assignment_id
            JOIN subjects sub ON sub.id = a.subject_id
            WHERE e.id = ? AND s.student_id = ? AND e.status = 'PUBLISHED'
            """;
        return Db.queryOne(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("evalId", rs.getInt("id"));
            m.put("assignmentTitle", rs.getString("assignment_title"));
            m.put("subjectCode", rs.getString("subject_code"));
            m.put("maxMarks", rs.getBigDecimal("max_marks"));
            m.put("total", rs.getBigDecimal("total"));
            m.put("latePenalty", rs.getBigDecimal("late_penalty"));
            m.put("finalMarks", rs.getBigDecimal("final_marks"));
            m.put("feedback", rs.getString("feedback"));
            m.put("isLate", rs.getBoolean("is_late"));
            m.put("receiptNo", rs.getString("receipt_no"));
            m.put("versionNo", rs.getInt("version_no"));
            m.put("submittedAt", TimeUtil.format(rs.getObject("submitted_at", LocalDateTime.class)));
            m.put("publishedAt", TimeUtil.format(rs.getObject("published_at", LocalDateTime.class)));
            return m;
        }, evalId, studentId);
    }

    /** Rubric breakdown of one evaluation: criterion name, max marks, marks given. */
    public List<Map<String,Object>> listScoreDetails(int evalId) {
        return Db.query("""
            SELECT rc.name, rc.max_marks, es.marks
            FROM evaluation_scores es
            JOIN rubric_criteria rc ON rc.id = es.criterion_id
            WHERE es.evaluation_id = ? ORDER BY rc.sort_order
            """, rs -> {
                Map<String,Object> m = new java.util.LinkedHashMap<>();
                m.put("name", rs.getString("name"));
                m.put("maxMarks", rs.getBigDecimal("max_marks"));
                m.put("marks", rs.getBigDecimal("marks"));
                return m;
            }, evalId);
    }

    /** One row per student of the assignment's section (submitted or not), for the CSV export. */
    public List<Map<String,Object>> listMarksForAssignment(int assignmentId) {
        String sql = """
            SELECT st.roll_no, u.name, cs.submitted_at, cs.is_late,
                   e.status AS eval_status, e.late_penalty, e.final_marks
            FROM assignments a
            JOIN students st ON st.section_id = a.section_id
            JOIN users u ON u.id = st.user_id
            LEFT JOIN current_submissions cs ON cs.assignment_id = a.id AND cs.student_id = st.user_id
            LEFT JOIN evaluations e ON e.submission_id = cs.id
            WHERE a.id = ?
            ORDER BY st.roll_no
            """;
        return Db.query(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("rollNo", rs.getString("roll_no"));
            m.put("name", rs.getString("name"));
            m.put("submittedAt", rs.getObject("submitted_at", LocalDateTime.class));
            m.put("isLate", rs.getBoolean("is_late"));
            m.put("evalStatus", rs.getString("eval_status"));
            m.put("latePenalty", rs.getBigDecimal("late_penalty"));
            m.put("finalMarks", rs.getBigDecimal("final_marks"));
            return m;
        }, assignmentId);
    }

    /** Average of final_marks / max_marks (in percent) over a teacher's PUBLISHED evaluations, or null. */
    public Double averagePercentForTeacher(int teacherId) {
        String sql = """
            SELECT AVG(e.final_marks / a.max_marks * 100)
            FROM evaluations e
            JOIN submissions s ON s.id = e.submission_id
            JOIN assignments a ON a.id = s.assignment_id
            WHERE a.teacher_id = ? AND e.status = 'PUBLISHED'
            """;
        return Db.queryOne(sql, rs -> {
            double v = rs.getDouble(1);
            return rs.wasNull() ? null : v;
        }, teacherId);
    }
}
