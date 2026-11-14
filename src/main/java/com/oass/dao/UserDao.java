package com.oass.dao;

import com.oass.model.*;
import com.oass.util.Db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data access for the users, students and teachers tables.
 * Called by LoginServlet, AuthFilter and the dashboard servlets.
 * Syllabus: Section C (JDBC, DAO pattern).
 */
public class UserDao {

    /** Maps a ResultSet row from the users table to a User object. */
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setStatus(UserStatus.valueOf(rs.getString("status")));
        u.setPhone(rs.getString("phone"));
        u.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return u;
    }

    /** Finds a user by email. Returns null if not found. */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return Db.queryOne(sql, this::mapUser, email);
    }

    /** Finds a user by id. Returns null if not found. */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return Db.queryOne(sql, this::mapUser, id);
    }

    /** Finds the Student detail row for a given user id. Returns null if not found. */
    public Student findStudentById(int userId) {
        String sql = """
            SELECT st.*, sec.name AS section_name
            FROM students st
            JOIN sections sec ON sec.id = st.section_id
            WHERE st.user_id = ?
            """;
        return Db.queryOne(sql, rs -> {
            Student s = new Student();
            s.setUserId(rs.getInt("user_id"));
            s.setRollNo(rs.getString("roll_no"));
            s.setSectionId(rs.getInt("section_id"));
            s.setSectionName(rs.getString("section_name"));
            return s;
        }, userId);
    }

    /** Finds the Teacher detail row for a given user id. Returns null if not found. */
    public Teacher findTeacherById(int userId) {
        String sql = "SELECT * FROM teachers WHERE user_id = ?";
        return Db.queryOne(sql, rs -> {
            Teacher t = new Teacher();
            t.setUserId(rs.getInt("user_id"));
            t.setEmployeeId(rs.getString("employee_id"));
            t.setDepartment(rs.getString("department"));
            return t;
        }, userId);
    }

    /** Returns the count of unread notifications for a user. */
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1), userId);
        return n == null ? 0 : n;
    }

    /** Returns the total number of ACTIVE users. */
    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'";
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1));
        return n == null ? 0 : n;
    }

    /** Returns the count of PENDING teachers awaiting approval. */
    public int countPendingTeachers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'TEACHER' AND status = 'PENDING'";
        Integer n = Db.queryOne(sql, rs -> rs.getInt(1));
        return n == null ? 0 : n;
    }
}
