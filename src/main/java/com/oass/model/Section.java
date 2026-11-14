package com.oass.model;

/**
 * Maps to the sections table.
 * A section is one class group, e.g. CSE-5D, Batch 2024, Semester 5.
 * Syllabus: Section A (OOP, encapsulation).
 */
public class Section {

    private int id;
    private String course;
    private String name;
    private int batchYear;
    private int semester;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBatchYear() { return batchYear; }
    public void setBatchYear(int batchYear) { this.batchYear = batchYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    /** Returns a readable label like "CSE-5D (B.Tech CSE, Batch 2024, Sem 5)". */
    public String getLabel() {
        return name + " (" + course + ", Batch " + batchYear + ", Sem " + semester + ")";
    }
}
