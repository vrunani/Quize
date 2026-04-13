package com.jfsd.quize.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "exam_code", unique = true)
    private String examCode;

    @Column(name = "created_by")
private String createdBy;

public String getCreatedBy() { return createdBy; }
public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @Column(name = "total_marks")
    private int totalMarks;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "negative_marking")
    private BigDecimal negativeMarking = BigDecimal.ZERO;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "show_results")
    private Boolean showResults = false;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // ✅ Getters & Setters
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }


    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
}
