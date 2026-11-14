package com.oass.dao;

import com.oass.model.*;
import com.oass.util.Db;
import com.oass.util.TimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data access for the assignments table.
 * Used by the student and teacher dashboards in P1.
 * Syllabus: Section C (JDBC, DAO pattern).
 */
public class AssignmentDao {

    /** Maps one ResultSet row (with subject/section joins) to an Assignment. */
    private Assignment mapAssignment(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setId(rs.getInt("id"));
        a.setTeacherId(rs.getInt("teacher_id"));
        a.setSubjectId(rs.getInt("subject_id"));
        a.setSectionId(rs.getInt("section_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setMaxMarks(rs.getBigDecimal("max_marks"));
        a.setStatus(AssignmentStatus.valueOf(rs.getString("status")));
        a.setDeadline(rs.getObject("deadline", LocalDateTime.class));
        a.setAllowLate(rs.getBoolean("allow_late"));
        a.setLatePenaltyPctPerDay(rs.getBigDecimal("late_penalty_pct_per_day"));
        a.setMaxLatePenaltyPct(rs.getBigDecimal("max_late_penalty_pct"));
        a.setHardCutoff(rs.getObject("hard_cutoff", LocalDateTime.class));
        a.setAllowedTypes(rs.getString("allowed_types"));
        a.setMaxFileMb(rs.getInt("max_file_mb"));
        a.setPlagiarismEnabled(rs.getBoolean("plagiarism_enabled"));
        a.setPlagiarismThreshold(rs.getBigDecimal("plagiarism_threshold"));
        a.setBlindGrading(rs.getBoolean("blind_grading"));
        a.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

        // display fields from joins
        try { a.setSubjectName(rs.getString("subject_name")); } catch (SQLException ignored) {}
        try { a.setSubjectCode(rs.getString("subject_code")); } catch (SQLException ignored) {}
        try { a.setSectionName(rs.getString("section_name")); } catch (SQLException ignored) {}

        // time left text (server-computed, no JavaScript)
        if (a.getDeadline() != null) {
            String[] tl = TimeUtil.timeLeft(a.getDeadline());
            a.setTimeLeftText(tl[0]);
            a.setTimeLeftCssClass(tl[1]);
            a.setDeadlineText(TimeUtil.format(a.getDeadline()));
        }
        return a;
    }

    private static final String SELECT_WITH_JOINS =
            """
            SELECT a.*, sub.name AS subject_name, sub.code AS subject_code,
                   sec.name AS section_name
            FROM assignments a
            JOIN subjects sub ON sub.id = a.subject_id
            JOIN sections sec ON sec.id = a.section_id
            """;

    /**
     * Returns all PUBLISHED and CLOSED assignments for the given section, ordered by deadline.
     * DRAFT assignments are hidden from students. CLOSED ones still show in history/stats.
     */
    public List<Assignment> listVisibleForSection(int sectionId) {
        String sql = SELECT_WITH_JOINS
                + "WHERE a.section_id = ? AND a.status != 'DRAFT' ORDER BY a.deadline";
        return Db.query(sql, this::mapAssignment, sectionId);
    }

    /** Returns only PUBLISHED assignments for the section (still accepting submissions). */
    public List<Assignment> listPublishedForSection(int sectionId) {
        String sql = SELECT_WITH_JOINS
                + "WHERE a.section_id = ? AND a.status = 'PUBLISHED' ORDER BY a.deadline";
        return Db.query(sql, this::mapAssignment, sectionId);
    }

    /** Returns all assignments created by a teacher, ordered by deadline desc. */
    public List<Assignment> listByTeacher(int teacherId) {
        String sql = SELECT_WITH_JOINS
                + "WHERE a.teacher_id = ? ORDER BY a.deadline DESC";
        return Db.query(sql, this::mapAssignment, teacherId);
    }

    /** Returns a single assignment by id, or null. */
    public Assignment findById(int id) {
        String sql = SELECT_WITH_JOINS + "WHERE a.id = ?";
        return Db.queryOne(sql, this::mapAssignment, id);
    }

    /** Counts total assignments for a teacher. */
    public int countByTeacher(int teacherId) {
        String sql = "SELECT COUNT(*) FROM assignments WHERE teacher_id = ?";
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1), teacherId);
        return n == null ? 0 : n;
    }

    /** Counts total assignments in all sections. */
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM assignments";
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1));
        return n == null ? 0 : n;
    }

    /**
     * Saves a new assignment together with its rubric criteria in ONE transaction:
     * if any criterion fails to save, the assignment is rolled back too.
     *
     * @return the id of the new assignment
     */
    public long saveWithRubric(Assignment a, List<String> criterionNames, List<java.math.BigDecimal> criterionMarks) {
        String assignmentSql = """
            INSERT INTO assignments (teacher_id, subject_id, section_id, title, description,
                max_marks, status, deadline, allow_late, late_penalty_pct_per_day,
                max_late_penalty_pct, hard_cutoff, allowed_types, max_file_mb,
                plagiarism_enabled, plagiarism_threshold, blind_grading)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        return Db.transaction(tx -> {
            long assignmentId = tx.insert(assignmentSql,
                a.getTeacherId(), a.getSubjectId(), a.getSectionId(), a.getTitle(), a.getDescription(),
                a.getMaxMarks(), a.getStatus().name(),
                a.getDeadline(), a.isAllowLate(), a.getLatePenaltyPctPerDay(),
                a.getMaxLatePenaltyPct(), a.getHardCutoff(), a.getAllowedTypes(),
                a.getMaxFileMb(), a.isPlagiarismEnabled(), a.getPlagiarismThreshold(), a.isBlindGrading());

            for (int i = 0; i < criterionNames.size(); i++) {
                tx.update("INSERT INTO rubric_criteria (assignment_id, name, max_marks, sort_order) VALUES (?,?,?,?)",
                    assignmentId, criterionNames.get(i), criterionMarks.get(i), i + 1);
            }
            return assignmentId;
        });
    }

    /**
     * Lists all assignments created by a teacher, joined with subject/section and submission count.
     * Returns each row as a Map for easy use in JSPs.
     */
    public List<Map<String,Object>> listByTeacherWithCounts(int teacherId) {
        String sql = """
            SELECT a.*, sub.name AS subject_name, sub.code AS subject_code,
                   sec.name AS section_name,
                   COUNT(cs.id) AS sub_count,
                   (SELECT COUNT(*) FROM students st WHERE st.section_id = a.section_id) AS total_students
            FROM assignments a
            JOIN subjects sub ON sub.id = a.subject_id
            JOIN sections sec ON sec.id = a.section_id
            LEFT JOIN current_submissions cs ON cs.assignment_id = a.id
            WHERE a.teacher_id = ?
            GROUP BY a.id
            ORDER BY a.deadline DESC
            """;
        return Db.query(sql, rs -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("title", rs.getString("title"));
            m.put("subjectCode", rs.getString("subject_code"));
            m.put("sectionName", rs.getString("section_name"));
            m.put("maxMarks", rs.getBigDecimal("max_marks"));
            m.put("deadline", com.oass.util.TimeUtil.format(rs.getObject("deadline", java.time.LocalDateTime.class)));
            m.put("status", rs.getString("status"));
            m.put("subCount", rs.getInt("sub_count"));
            m.put("totalStudents", rs.getInt("total_students"));
            return m;
        }, teacherId);
    }
}
