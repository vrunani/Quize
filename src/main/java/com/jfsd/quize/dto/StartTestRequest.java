package com.jfsd.quize.dto;

public class StartTestRequest {
    private String examCode;   // student uses examCode to enter test
    private String studentId;

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}