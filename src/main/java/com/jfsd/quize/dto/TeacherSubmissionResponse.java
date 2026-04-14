package com.jfsd.quize.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Returned by GET /submissions/test/{testId}
 * Gives the teacher a per-student summary plus all their answers
 * so the grading UI and results matrix can both be powered by
 * one endpoint call.
 */
public class TeacherSubmissionResponse {

    private Long          submissionId;
    private String        studentId;
    private String        studentName;    // joined from users table
    private String        studentEmail;
    private String        status;         // IN_PROGRESS | SUBMITTED | EVALUATED
    private BigDecimal    totalScore;
    private int           totalMarks;     // from the test
    private double        percentage;
    private LocalDateTime submittedAt;
    private List<AnswerDetail> answers;   // full answer breakdown

    // ── inner class ───────────────────────────────────────────
    public static class AnswerDetail {
        private Long       answerId;
        private int        questionNumber;
        private String     questionText;
        private String     questionType;   // OBJECTIVE | SUBJECTIVE
        private Integer    selectedOption;
        private String     textAnswer;
        private Integer    correctOption;  // null for SUBJECTIVE
        private int        maxMarks;
        private BigDecimal marksAwarded;
        private String     answerStatus;   // AUTO | PENDING | CHECKED | MANUAL
        private String     checkedBy;
        private LocalDateTime checkedAt;

        // getters & setters
        public Long       getAnswerId()                    { return answerId; }
        public void       setAnswerId(Long v)              { this.answerId = v; }

        public int        getQuestionNumber()              { return questionNumber; }
        public void       setQuestionNumber(int v)         { this.questionNumber = v; }

        public String     getQuestionText()                { return questionText; }
        public void       setQuestionText(String v)        { this.questionText = v; }

        public String     getQuestionType()                { return questionType; }
        public void       setQuestionType(String v)        { this.questionType = v; }

        public Integer    getSelectedOption()              { return selectedOption; }
        public void       setSelectedOption(Integer v)     { this.selectedOption = v; }

        public String     getTextAnswer()                  { return textAnswer; }
        public void       setTextAnswer(String v)          { this.textAnswer = v; }

        public Integer    getCorrectOption()               { return correctOption; }
        public void       setCorrectOption(Integer v)      { this.correctOption = v; }

        public int        getMaxMarks()                    { return maxMarks; }
        public void       setMaxMarks(int v)               { this.maxMarks = v; }

        public BigDecimal getMarksAwarded()                { return marksAwarded; }
        public void       setMarksAwarded(BigDecimal v)    { this.marksAwarded = v; }

        public String     getAnswerStatus()                { return answerStatus; }
        public void       setAnswerStatus(String v)        { this.answerStatus = v; }

        public String     getCheckedBy()                   { return checkedBy; }
        public void       setCheckedBy(String v)           { this.checkedBy = v; }

        public LocalDateTime getCheckedAt()                { return checkedAt; }
        public void          setCheckedAt(LocalDateTime v) { this.checkedAt = v; }
    }

    // ── outer getters & setters ───────────────────────────────
    public Long getSubmissionId()                   { return submissionId; }
    public void setSubmissionId(Long v)             { this.submissionId = v; }

    public String getStudentId()                    { return studentId; }
    public void   setStudentId(String v)            { this.studentId = v; }

    public String getStudentName()                  { return studentName; }
    public void   setStudentName(String v)          { this.studentName = v; }

    public String getStudentEmail()                 { return studentEmail; }
    public void   setStudentEmail(String v)         { this.studentEmail = v; }

    public String getStatus()                       { return status; }
    public void   setStatus(String v)               { this.status = v; }

    public BigDecimal getTotalScore()               { return totalScore; }
    public void       setTotalScore(BigDecimal v)   { this.totalScore = v; }

    public int  getTotalMarks()                     { return totalMarks; }
    public void setTotalMarks(int v)                { this.totalMarks = v; }

    public double getPercentage()                   { return percentage; }
    public void   setPercentage(double v)           { this.percentage = v; }

    public LocalDateTime getSubmittedAt()             { return submittedAt; }
    public void          setSubmittedAt(LocalDateTime v) { this.submittedAt = v; }

    public List<AnswerDetail> getAnswers()                    { return answers; }
    public void               setAnswers(List<AnswerDetail> v) { this.answers = v; }
}