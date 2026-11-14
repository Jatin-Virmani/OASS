package com.oass.dao;

import com.oass.model.Section;
import com.oass.util.Db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access for sections, subjects, and the teaching mapping table.
 * Used by registration flows and assignment creation.
 * Syllabus: Section C (JDBC, DAO pattern).
 */
public class AcademicDao {

    /** Returns all sections ordered by course and name. */
    public List<Section> listSections() {
        String sql = "SELECT * FROM sections ORDER BY course, name";
        return Db.query(sql, rs -> {
            Section s = new Section();
            s.setId(rs.getInt("id"));
            s.setCourse(rs.getString("course"));
            s.setName(rs.getString("name"));
            s.setBatchYear(rs.getInt("batch_year"));
            s.setSemester(rs.getInt("semester"));
            return s;
        });
    }

    /** Counts total sections. */
    public int countSections() {
        Integer n = Db.queryOne("SELECT COUNT(*) FROM sections", rs -> rs.getInt(1));
        return n == null ? 0 : n;
    }

    /** The subject + section pairs a teacher teaches (rows of the teaching table). */
    public List<Map<String, Object>> listTeachingForTeacher(int teacherId) {
        return Db.query(TEACHING_SQL + "WHERE t.teacher_id = ? ORDER BY sub.code, sec.name",
                this::mapTeaching, teacherId);
    }

    /** One teaching row of this teacher, or null. Stops a teacher using a class that is not theirs. */
    public Map<String, Object> findTeaching(int teacherId, int teachingId) {
        return Db.queryOne(TEACHING_SQL + "WHERE t.teacher_id = ? AND t.id = ?",
                this::mapTeaching, teacherId, teachingId);
    }

    /** The distinct subjects a teacher teaches (id, code, name). */
    public List<Map<String, Object>> listSubjectsForTeacher(int teacherId) {
        return Db.query("""
            SELECT DISTINCT sub.id, sub.code, sub.name
            FROM teaching t JOIN subjects sub ON sub.id = t.subject_id
            WHERE t.teacher_id = ? ORDER BY sub.code
            """, rs -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("code", rs.getString("code"));
                m.put("name", rs.getString("name"));
                return m;
            }, teacherId);
    }

    /** The distinct sections a teacher teaches (id, name). */
    public List<Map<String, Object>> listSectionsForTeacher(int teacherId) {
        return Db.query("""
            SELECT DISTINCT sec.id, sec.name
            FROM teaching t JOIN sections sec ON sec.id = t.section_id
            WHERE t.teacher_id = ? ORDER BY sec.name
            """, rs -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("name", rs.getString("name"));
                return m;
            }, teacherId);
    }

    private static final String TEACHING_SQL = """
        SELECT t.id, sub.id AS subject_id, sub.code AS subject_code, sub.name AS subject_name,
               sec.id AS section_id, sec.name AS section_name
        FROM teaching t
        JOIN subjects sub ON sub.id = t.subject_id
        JOIN sections sec ON sec.id = t.section_id
        """;

    private Map<String, Object> mapTeaching(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("teachingId", rs.getInt("id"));
        m.put("subjectId", rs.getInt("subject_id"));
        m.put("subjectCode", rs.getString("subject_code"));
        m.put("subjectName", rs.getString("subject_name"));
        m.put("sectionId", rs.getInt("section_id"));
        m.put("sectionName", rs.getString("section_name"));
        return m;
    }
}
