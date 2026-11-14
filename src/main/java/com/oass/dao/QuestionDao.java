package com.oass.dao;

import com.oass.util.Db;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data access for the question bank (questions table). */
public class QuestionDao {

    /** Adds a question to the teacher's own bank. */
    public void add(int teacherId, int subjectId, String type, String text,
                    BigDecimal marks, String difficulty, String topic) {
        Db.update("""
            INSERT INTO questions (teacher_id, subject_id, question_type, question_text, marks, difficulty, topic)
            VALUES (?,?,?,?,?,?,?)
            """, teacherId, subjectId, type, text, marks, difficulty, topic);
    }

    /** All questions of one teacher, newest first. */
    public List<Map<String, Object>> listForTeacher(int teacherId) {
        return Db.query("""
            SELECT q.*, sub.code AS subject_code
            FROM questions q JOIN subjects sub ON sub.id = q.subject_id
            WHERE q.teacher_id = ? ORDER BY q.id DESC
            """, rs -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("subjectCode", rs.getString("subject_code"));
                m.put("topic", rs.getString("topic"));
                m.put("type", rs.getString("question_type"));
                m.put("text", rs.getString("question_text"));
                m.put("difficulty", rs.getString("difficulty"));
                m.put("marks", rs.getBigDecimal("marks"));
                return m;
            }, teacherId);
    }
}
