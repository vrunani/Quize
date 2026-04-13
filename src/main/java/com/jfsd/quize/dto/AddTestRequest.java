package com.jfsd.quize.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AddTestRequest {
    private String title;
    private String description;
    private String examCode;
    private String createdBy;
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    private Integer  totalMarks;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal negativeMarking;
    private Boolean isPublished;
    private Boolean showResults;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

public Integer getTotalMarks() { return totalMarks; }
public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }


public Integer getDurationMinutes() { return durationMinutes; }
public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(BigDecimal negativeMarking) { this.negativeMarking = negativeMarking; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public Boolean getShowResults() { return showResults; }
    public void setShowResults(Boolean showResults) { this.showResults = showResults; }
}

