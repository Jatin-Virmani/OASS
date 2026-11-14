package com.oass.dao;

import com.oass.util.Db;
import com.oass.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data access for the announcements table. */
public class AnnouncementDao {

    /** Posts an announcement to one section. */
    public void add(int teacherId, int sectionId, String title, String body) {
        Db.update("INSERT INTO announcements (teacher_id, section_id, title, body) VALUES (?,?,?,?)",
                teacherId, sectionId, title, body);
    }

    /** Announcements posted by one teacher, newest first. */
    public List<Map<String, Object>> listForTeacher(int teacherId) {
        return Db.query("""
            SELECT an.*, sec.name AS section_name
            FROM announcements an JOIN sections sec ON sec.id = an.section_id
            WHERE an.teacher_id = ? ORDER BY an.created_at DESC, an.id DESC
            """, this::mapRow, teacherId);
    }

    /** The latest announcements for one section (shown on the student dashboard). */
    public List<Map<String, Object>> listForSection(int sectionId, int limit) {
        return Db.query("""
            SELECT an.*, sec.name AS section_name
            FROM announcements an JOIN sections sec ON sec.id = an.section_id
            WHERE an.section_id = ? ORDER BY an.created_at DESC, an.id DESC LIMIT ?
            """, this::mapRow, sectionId, limit);
    }

    private Map<String, Object> mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", rs.getString("title"));
        m.put("body", rs.getString("body"));
        m.put("sectionName", rs.getString("section_name"));
        m.put("createdAt", TimeUtil.format(rs.getObject("created_at", LocalDateTime.class)));
        return m;
    }
}
