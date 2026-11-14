package com.oass.model;

import java.time.LocalDateTime;

/**
 * Maps to the notifications table.
 * Bell icon shows unreadCount; this list page shows all notifications.
 * Syllabus: Section A (OOP, encapsulation).
 */
public class Notification {

    private int id;
    private int userId;
    private String title;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;

    // display field
    private String createdAtText;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedAtText() { return createdAtText; }
    public void setCreatedAtText(String createdAtText) { this.createdAtText = createdAtText; }
}
