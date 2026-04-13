package com.jfsd.quize.dto;

public class SaveAnswerRequest {
    private Long submissionId;
    private String examCode;       // examCode instead of testId
    private Integer questionNumber;
    private Integer selectedOption;  // for objective
    private String textAnswer;       // for subjective

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }

    public Integer getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Integer selectedOption) { this.selectedOption = selectedOption; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
}