package com.oass.dao;

import com.oass.util.Db;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data access for similarity_results and the submissions that are compared. */
public class PlagiarismDao {

    /** Current submissions (with text) of this teacher's plagiarism-enabled assignments. */
    public List<Map<String, Object>> listComparable(int teacherId) {
        String sql = """
            SELECT cs.id, cs.assignment_id, cs.student_id, cs.text_content, a.plagiarism_threshold
            FROM current_submissions cs
            JOIN assignments a ON a.id = cs.assignment_id
            WHERE a.teacher_id = ? AND a.plagiarism_enabled = 1
              AND cs.text_content IS NOT NULL AND cs.text_content <> ''
            ORDER BY cs.assignment_id, cs.id
            """;
        return Db.query(sql, rs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("assignmentId", rs.getInt("assignment_id"));
            m.put("studentId", rs.getInt("student_id"));
            m.put("text", rs.getString("text_content"));
            m.put("threshold", rs.getBigDecimal("plagiarism_threshold").doubleValue());
            return m;
        }, teacherId);
    }

    /** Inserts a pair result, or updates it if the pair was compared before. submissionA must be the smaller id. */
    public void saveResult(int assignmentId, int submissionA, int submissionB,
                           BigDecimal jaccard, BigDecimal cosine, BigDecimal score, String risk) {
        Db.update("""
            INSERT INTO similarity_results (assignment_id, submission_a, submission_b, jaccard, cosine, score, risk)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE jaccard = VALUES(jaccard), cosine = VALUES(cosine),
                                    score = VALUES(score), risk = VALUES(risk)
            """, assignmentId, submissionA, submissionB, jaccard, cosine, score, risk);
    }

    /** All compared pairs of this teacher's assignments, highest score first. */
    public List<Map<String, Object>> listResults(int teacherId) {
        String sql = """
            SELECT sr.*, a.title AS assignment_title,
                   u1.name AS student_a_name, u2.name AS student_b_name
            FROM similarity_results sr
            JOIN assignments a ON a.id = sr.assignment_id
            JOIN submissions s1 ON s1.id = sr.submission_a
            JOIN users u1 ON u1.id = s1.student_id
            JOIN submissions s2 ON s2.id = sr.submission_b
            JOIN users u2 ON u2.id = s2.student_id
            WHERE a.teacher_id = ?
            ORDER BY sr.score DESC
            """;
        return Db.query(sql, rs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("assignmentTitle", rs.getString("assignment_title"));
            m.put("studentAName", rs.getString("student_a_name"));
            m.put("studentBName", rs.getString("student_b_name"));
            m.put("jaccard", rs.getBigDecimal("jaccard"));
            m.put("cosine", rs.getBigDecimal("cosine"));
            m.put("score", rs.getBigDecimal("score"));
            m.put("risk", rs.getString("risk"));
            return m;
        }, teacherId);
    }

    /** One pair with both texts, or null if it does not exist or belongs to another teacher. */
    public Map<String, Object> findPair(int pairId, int teacherId) {
        String sql = """
            SELECT sr.*, a.title AS assignment_title,
                   u1.name AS student_a_name, s1.text_content AS text_a, s1.receipt_no AS receipt_a,
                   u2.name AS student_b_name, s2.text_content AS text_b, s2.receipt_no AS receipt_b
            FROM similarity_results sr
            JOIN assignments a ON a.id = sr.assignment_id
            JOIN submissions s1 ON s1.id = sr.submission_a
            JOIN users u1 ON u1.id = s1.student_id
            JOIN submissions s2 ON s2.id = sr.submission_b
            JOIN users u2 ON u2.id = s2.student_id
            WHERE sr.id = ? AND a.teacher_id = ?
            """;
        return Db.queryOne(sql, rs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("assignmentTitle", rs.getString("assignment_title"));
            m.put("studentAName", rs.getString("student_a_name"));
            m.put("studentBName", rs.getString("student_b_name"));
            m.put("textA", rs.getString("text_a") == null ? "" : rs.getString("text_a"));
            m.put("textB", rs.getString("text_b") == null ? "" : rs.getString("text_b"));
            m.put("receiptA", rs.getString("receipt_a"));
            m.put("receiptB", rs.getString("receipt_b"));
            m.put("jaccard", rs.getBigDecimal("jaccard"));
            m.put("cosine", rs.getBigDecimal("cosine"));
            m.put("score", rs.getBigDecimal("score"));
            m.put("risk", rs.getString("risk"));
            return m;
        }, pairId, teacherId);
    }
}
