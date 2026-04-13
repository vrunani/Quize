package com.jfsd.quize.dto;

import com.jfsd.quize.entity.Question.QuestionType;

public class QuestionRequest {

    private String examCode;   // ← replaces testId; all question ops use examCode
    private String questionText;
    private QuestionType questionType;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private Integer correctOption;
    private int marks;

    // Getters & Setters
    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public Integer getCorrectOption() { return correctOption; }
    public void setCorrectOption(Integer correctOption) { this.correctOption = correctOption; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
}
