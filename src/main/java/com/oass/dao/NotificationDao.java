package com.oass.dao;

import com.oass.model.Notification;
import com.oass.util.Db;
import com.oass.util.TimeUtil;

import java.util.List;

/**
 * Data access for the notifications table.
 * Used by layout.tag (unread count) and the notifications page.
 * Syllabus: Section C (JDBC, DAO pattern).
 */
public class NotificationDao {

    /** Returns up to 20 most recent notifications for a user, newest first. */
    public List<Notification> listForUser(int userId) {
        String sql = """
            SELECT * FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT 20
            """;
        return Db.query(sql, rs -> {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setUserId(rs.getInt("user_id"));
            n.setTitle(rs.getString("title"));
            n.setMessage(rs.getString("message"));
            n.setLink(rs.getString("link"));
            n.setRead(rs.getBoolean("is_read"));
            n.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
            n.setCreatedAtText(TimeUtil.format(n.getCreatedAt()));
            return n;
        }, userId);
    }

    /** Marks all notifications for a user as read. */
    public void markAllRead(int userId) {
        Db.update("UPDATE notifications SET is_read = 1 WHERE user_id = ?", userId);
    }
}
