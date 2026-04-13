package com.jfsd.quize.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "test_id", nullable = false)
    private Long testId;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "selected_option")
    private Integer selectedOption;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "marks_awarded")
    private BigDecimal marksAwarded = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AnswerStatus status = AnswerStatus.PENDING;

    @Column(name = "checked_by")
    private String checkedBy;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    public enum AnswerStatus {
        AUTO, PENDING, CHECKED, MANUAL
    }

    // Getters & Setters
    public Long getId() { return id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }

    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }

    public Integer getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Integer selectedOption) { this.selectedOption = selectedOption; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }

    public BigDecimal getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(BigDecimal marksAwarded) { this.marksAwarded = marksAwarded; }

    public AnswerStatus getStatus() { return status; }
    public void setStatus(AnswerStatus status) { this.status = status; }

    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}