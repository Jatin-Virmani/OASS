package com.oass.model;

import java.time.LocalDateTime;

/**
 * Maps to the assignments table. One assignment targets one section.
 * Non-DB display fields: subjectName, sectionName, timeLeftText, stateLabel.
 * Syllabus: Section A (OOP, encapsulation).
 */
public class Assignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int sectionId;
    private String title;
    private String description;
    private java.math.BigDecimal maxMarks;
    private AssignmentStatus status;
    private LocalDateTime deadline;
    private boolean allowLate;
    private java.math.BigDecimal latePenaltyPctPerDay;
    private java.math.BigDecimal maxLatePenaltyPct;
    private LocalDateTime hardCutoff;
    private String allowedTypes;
    private int maxFileMb;
    private boolean plagiarismEnabled;
    private java.math.BigDecimal plagiarismThreshold;
    private boolean blindGrading;
    private LocalDateTime createdAt;

    // display fields – filled by DAO joins
    private String subjectName;
    private String subjectCode;
    private String sectionName;
    private String timeLeftText;       // e.g. "Due in 2h 14m"
    private String timeLeftCssClass;   // e.g. "time-urgent"
    private String deadlineText;       // formatted dd MMM yyyy, hh:mm a

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.math.BigDecimal getMaxMarks() { return maxMarks; }
    public void setMaxMarks(java.math.BigDecimal maxMarks) { this.maxMarks = maxMarks; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public boolean isAllowLate() { return allowLate; }
    public void setAllowLate(boolean allowLate) { this.allowLate = allowLate; }

    public java.math.BigDecimal getLatePenaltyPctPerDay() { return latePenaltyPctPerDay; }
    public void setLatePenaltyPctPerDay(java.math.BigDecimal v) { this.latePenaltyPctPerDay = v; }

    public java.math.BigDecimal getMaxLatePenaltyPct() { return maxLatePenaltyPct; }
    public void setMaxLatePenaltyPct(java.math.BigDecimal v) { this.maxLatePenaltyPct = v; }

    public LocalDateTime getHardCutoff() { return hardCutoff; }
    public void setHardCutoff(LocalDateTime hardCutoff) { this.hardCutoff = hardCutoff; }

    public String getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(String allowedTypes) { this.allowedTypes = allowedTypes; }

    public int getMaxFileMb() { return maxFileMb; }
    public void setMaxFileMb(int maxFileMb) { this.maxFileMb = maxFileMb; }

    public boolean isPlagiarismEnabled() { return plagiarismEnabled; }
    public void setPlagiarismEnabled(boolean plagiarismEnabled) { this.plagiarismEnabled = plagiarismEnabled; }

    public java.math.BigDecimal getPlagiarismThreshold() { return plagiarismThreshold; }
    public void setPlagiarismThreshold(java.math.BigDecimal v) { this.plagiarismThreshold = v; }

    public boolean isBlindGrading() { return blindGrading; }
    public void setBlindGrading(boolean blindGrading) { this.blindGrading = blindGrading; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getTimeLeftText() { return timeLeftText; }
    public void setTimeLeftText(String timeLeftText) { this.timeLeftText = timeLeftText; }

    public String getTimeLeftCssClass() { return timeLeftCssClass; }
    public void setTimeLeftCssClass(String timeLeftCssClass) { this.timeLeftCssClass = timeLeftCssClass; }

    public String getDeadlineText() { return deadlineText; }
    public void setDeadlineText(String deadlineText) { this.deadlineText = deadlineText; }

    /** Deadline as epoch milliseconds, read by the JavaScript countdown (data-deadline). */
    public long getDeadlineEpochMillis() {
        return deadline == null ? 0 : deadline.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
