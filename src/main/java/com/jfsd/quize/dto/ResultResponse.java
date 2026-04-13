package com.jfsd.quize.dto;

import java.math.BigDecimal;
import java.util.List;

public class ResultResponse {
    private Long submissionId;
    private Long testId;
    private String studentId;
    private BigDecimal totalScore;
    private int totalMarks;
    private List<QuestionResult> questions;

    public static class QuestionResult {
        private Integer questionNumber;
        private String questionText;
        private String questionType;
        private Integer correctOption;     // null for subjective
        private Integer selectedOption;    // null for subjective
        private String textAnswer;         // null for objective
        private BigDecimal marksAwarded;
        private int maxMarks;
        private String answerStatus;       // AUTO, PENDING, CHECKED

        // Getters & Setters
        public Integer getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public Integer getCorrectOption() { return correctOption; }
        public void setCorrectOption(Integer correctOption) { this.correctOption = correctOption; }

        public Integer getSelectedOption() { return selectedOption; }
        public void setSelectedOption(Integer selectedOption) { this.selectedOption = selectedOption; }

        public String getTextAnswer() { return textAnswer; }
        public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }

        public BigDecimal getMarksAwarded() { return marksAwarded; }
        public void setMarksAwarded(BigDecimal marksAwarded) { this.marksAwarded = marksAwarded; }

        public int getMaxMarks() { return maxMarks; }
        public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

        public String getAnswerStatus() { return answerStatus; }
        public void setAnswerStatus(String answerStatus) { this.answerStatus = answerStatus; }
    }

    // Getters & Setters
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public List<QuestionResult> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResult> questions) { this.questions = questions; }
}