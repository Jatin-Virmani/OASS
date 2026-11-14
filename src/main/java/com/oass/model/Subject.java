package com.oass.model;

/**
 * Maps to the subjects table.
 * e.g. code=CS505, name="Advanced Java".
 * Syllabus: Section A (OOP, encapsulation).
 */
public class Subject {

    private int id;
    private String code;
    private String name;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
