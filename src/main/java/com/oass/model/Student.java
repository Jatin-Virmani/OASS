package com.oass.model;

/**
 * Maps to the students table. user_id is the FK to users.
 * Carries non-DB display fields: sectionName populated by the DAO join.
 * Syllabus: Section A (OOP, encapsulation).
 */
public class Student {

    private int userId;
    private String rollNo;
    private int sectionId;

    // display field – filled by DAO join (not a DB column)
    private String sectionName;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
}
