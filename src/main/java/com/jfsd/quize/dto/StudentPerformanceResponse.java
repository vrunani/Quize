package com.jfsd.quize.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One entry in the student performance graph.
 * Returned as a List<StudentPerformanceResponse> by:
 *   GET /analytics/student/{studentId}
 *   GET /student/{studentId}/performance
 *
 * The frontend uses:
 *   testTitle     → X-axis label
 *   percentage    → Y-axis bar height
 *   marksObtained / totalMarks → bar tooltip
 */
public class StudentPerformanceResponse {

    private Long          testId;
    private String        examCode;
    private String        testTitle;
    private int           totalMarks;
    private BigDecimal    marksObtained;   // = submission.totalScore
    private double        percentage;      // = (marksObtained / totalMarks) * 100
    private String        status;          // SUBMITTED | EVALUATED
    private LocalDateTime submittedAt;

    // ── constructors ──────────────────────────────────────────
    public StudentPerformanceResponse() {}

    public StudentPerformanceResponse(Long testId, String examCode, String testTitle,
                                       int totalMarks, BigDecimal marksObtained,
                                       String status, LocalDateTime submittedAt) {
        this.testId        = testId;
        this.examCode      = examCode;
        this.testTitle     = testTitle;
        this.totalMarks    = totalMarks;
        this.marksObtained = marksObtained;
        this.status        = status;
        this.submittedAt   = submittedAt;
        this.percentage    = totalMarks > 0
            ? marksObtained.doubleValue() / totalMarks * 100.0
            : 0.0;
    }

    // ── getters & setters ─────────────────────────────────────
    public Long getTestId()                { return testId; }
    public void setTestId(Long testId)     { this.testId = testId; }

    public String getExamCode()              { return examCode; }
    public void   setExamCode(String v)      { this.examCode = v; }

    public String getTestTitle()             { return testTitle; }
    public void   setTestTitle(String v)     { this.testTitle = v; }

    public int  getTotalMarks()              { return totalMarks; }
    public void setTotalMarks(int v)         { this.totalMarks = v; }

    public BigDecimal getMarksObtained()              { return marksObtained; }
    public void       setMarksObtained(BigDecimal v)  { this.marksObtained = v; }

    public double getPercentage()            { return percentage; }
    public void   setPercentage(double v)    { this.percentage = v; }

    public String getStatus()              { return status; }
    public void   setStatus(String v)      { this.status = v; }

    public LocalDateTime getSubmittedAt()              { return submittedAt; }
    public void          setSubmittedAt(LocalDateTime v) { this.submittedAt = v; }
}