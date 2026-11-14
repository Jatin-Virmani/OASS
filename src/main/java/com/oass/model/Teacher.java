package com.oass.model;

/**
 * Maps to the teachers table. user_id is the FK to users.
 * Syllabus: Section A (OOP, encapsulation).
 */
public class Teacher {

    private int userId;
    private String employeeId;
    private String department;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
