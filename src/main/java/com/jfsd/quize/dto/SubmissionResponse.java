package com.jfsd.quize.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubmissionResponse {
    private Long submissionId;
    private String examCode;      // examCode instead of testId
    private String studentId;
    private String status;
    private BigDecimal totalScore;
    private LocalDateTime submittedAt;

    public SubmissionResponse(Long submissionId, String examCode, String studentId,
                               String status, BigDecimal totalScore, LocalDateTime submittedAt) {
        this.submissionId = submissionId;
        this.examCode = examCode;
        this.studentId = studentId;
        this.status = status;
        this.totalScore = totalScore;
        this.submittedAt = submittedAt;
    }

    public Long getSubmissionId() { return submissionId; }
    public String getExamCode() { return examCode; }
    public String getStudentId() { return studentId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalScore() { return totalScore; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}