package com.jfsd.quize.controller;

import com.jfsd.quize.dto.GradeAnswerRequest;
import com.jfsd.quize.dto.TeacherSubmissionResponse;
import com.jfsd.quize.entity.Answer;
import com.jfsd.quize.entity.Question;
import com.jfsd.quize.entity.Submission;
import com.jfsd.quize.entity.Test;
import com.jfsd.quize.entity.User;
import com.jfsd.quize.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/submissions")
@CrossOrigin
public class SubmissionController {

    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private AnswerRepository     answerRepository;
    @Autowired private QuestionRepository   questionRepository;
    @Autowired private TestRepository       testRepository;
    @Autowired private UserRepository       userRepository;

    // ─────────────────────────────────────────────────────────────
    // GET SUBMISSION BY ID
    // GET /submissions/{submissionId}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/{submissionId}")
    public ResponseEntity<?> getSubmission(@PathVariable Long submissionId) {
        return submissionRepository.findById(submissionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Submission not found"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL SUBMISSIONS FOR A TEST  (Teacher view)
    // GET /submissions/test/{testId}
    //
    // FIX #4, #10: Returns full TeacherSubmissionResponse including
    // student name, email, score, percentage, and all answer details
    // so the grading UI and results matrix are fully powered.
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/test/{testId}")
    public ResponseEntity<?> getSubmissionsForTest(@PathVariable Long testId) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found");

        Test test = testOpt.get();
        List<Submission> submissions = submissionRepository.findByTestId(testId);
        List<Question>   questions   = questionRepository.findByTestIdOrderByQuestionNumberAsc(testId);

        List<TeacherSubmissionResponse> result = new ArrayList<>();

        for (Submission sub : submissions) {

            // Enrich with student info
            User student = userRepository.findById(sub.getStudentId()).orElse(null);

            List<Answer> answers = answerRepository.findBySubmissionId(sub.getId());
            List<TeacherSubmissionResponse.AnswerDetail> answerDetails = new ArrayList<>();

            for (Question q : questions) {
                TeacherSubmissionResponse.AnswerDetail detail =
                    new TeacherSubmissionResponse.AnswerDetail();
                detail.setQuestionNumber(q.getQuestionNumber());
                detail.setQuestionText(q.getQuestionText());
                detail.setQuestionType(q.getQuestionType().name());
                detail.setCorrectOption(q.getCorrectOption());
                detail.setMaxMarks(q.getMarks());

                answers.stream()
                    .filter(a -> a.getQuestionNumber().equals(q.getQuestionNumber()))
                    .findFirst()
                    .ifPresent(a -> {
                        detail.setAnswerId(a.getId());
                        detail.setSelectedOption(a.getSelectedOption());
                        detail.setTextAnswer(a.getTextAnswer());
                        detail.setMarksAwarded(a.getMarksAwarded());
                        detail.setAnswerStatus(a.getStatus().name());
                        detail.setCheckedBy(a.getCheckedBy());
                        detail.setCheckedAt(a.getCheckedAt());
                    });

                answerDetails.add(detail);
            }

            double pct = test.getTotalMarks() > 0
                ? sub.getTotalScore().doubleValue() / test.getTotalMarks() * 100.0
                : 0.0;

            TeacherSubmissionResponse dto = new TeacherSubmissionResponse();
            dto.setSubmissionId(sub.getId());
            dto.setStudentId(sub.getStudentId());
            dto.setStudentName(student  != null ? student.getName()  : sub.getStudentId());
            dto.setStudentEmail(student != null ? student.getEmail() : "");
            dto.setStatus(sub.getStatus().name());
            dto.setTotalScore(sub.getTotalScore());
            dto.setTotalMarks(test.getTotalMarks());
            dto.setPercentage(pct);
            dto.setSubmittedAt(sub.getSubmittedAt());
            dto.setAnswers(answerDetails);

            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────
    // SUBMIT TEST
    // PUT /submissions/submit/{submissionId}
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/submit/{submissionId}")
    @Transactional
    public ResponseEntity<String> submitTest(@PathVariable Long submissionId) {

        Optional<Submission> subOpt = submissionRepository.findById(submissionId);
        if (!subOpt.isPresent())
            return ResponseEntity.badRequest().body("Submission not found");

        Submission submission = subOpt.get();

        if (submission.getStatus() != Submission.SubmissionStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().body("Test already submitted");

        // Recalculate score from saved answers
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);
        BigDecimal total = answers.stream()
            .filter(a -> a.getMarksAwarded() != null)
            .map(Answer::getMarksAwarded)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setTotalScore(total);
        submissionRepository.save(submission);

        return ResponseEntity.ok("Test submitted successfully. Objective score: " + total);
    }

    // ─────────────────────────────────────────────────────────────
    // VIEW RESULT (Student)
    // GET /submissions/result/{submissionId}
    //
    // FIX #8: Returns correctAnswers, wrongAnswers count, and full
    // answers[] array with questionNumber, selectedOption,
    // textAnswer, marksAwarded — exactly what the student UI needs.
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/result/{submissionId}")
    public ResponseEntity<?> getResult(@PathVariable Long submissionId) {

        Optional<Submission> subOpt = submissionRepository.findById(submissionId);
        if (!subOpt.isPresent())
            return ResponseEntity.badRequest().body("Submission not found");

        Submission submission = subOpt.get();

        Optional<Test> testOpt = testRepository.findById(submission.getTestId());
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found");

        Test test = testOpt.get();

        if (!test.getShowResults())
            return ResponseEntity.status(403).body("Results are not published yet");

        if (submission.getStatus() == Submission.SubmissionStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().body("You have not submitted this test yet");

        List<Question> questions = questionRepository.findByTestIdOrderByQuestionNumberAsc(test.getId());
        List<Answer>   answers   = answerRepository.findBySubmissionId(submissionId);

        int correct = 0, wrong = 0;
        List<Map<String, Object>> answerList = new ArrayList<>();

        for (Question q : questions) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("questionNumber", q.getQuestionNumber());
            entry.put("questionText",   q.getQuestionText());
            entry.put("questionType",   q.getQuestionType().name());
            entry.put("maxMarks",       q.getMarks());
            entry.put("correctOption",  q.getCorrectOption());  // shown only after results published

            Optional<Answer> ans = answers.stream()
                .filter(a -> a.getQuestionNumber().equals(q.getQuestionNumber()))
                .findFirst();

            if (ans.isPresent()) {
                Answer a = ans.get();
                entry.put("answerId",       a.getId());
                entry.put("selectedOption", a.getSelectedOption());
                entry.put("textAnswer",     a.getTextAnswer());
                entry.put("marksAwarded",   a.getMarksAwarded());
                entry.put("answerStatus",   a.getStatus().name());

                // Count correct/wrong for OBJECTIVE only
                if (q.getQuestionType() == Question.QuestionType.OBJECTIVE) {
                    if (a.getSelectedOption() != null &&
                        a.getSelectedOption().equals(q.getCorrectOption())) {
                        correct++;
                    } else if (a.getSelectedOption() != null) {
                        wrong++;
                    }
                }
            } else {
                entry.put("answerId",       null);
                entry.put("selectedOption", null);
                entry.put("textAnswer",     null);
                entry.put("marksAwarded",   BigDecimal.ZERO);
                entry.put("answerStatus",   "NOT_ANSWERED");
            }

            answerList.add(entry);
        }

        // Build response map matching exactly what student.html expects
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("submissionId",   submission.getId());
        response.put("testId",         test.getId());
        response.put("testTitle",      test.getTitle());
        response.put("studentId",      submission.getStudentId());
        response.put("status",         submission.getStatus().name());
        response.put("marksObtained",  submission.getTotalScore());   // ← key student.html reads
        response.put("totalMarks",     test.getTotalMarks());
        response.put("correctAnswers", correct);                      // ← FIX #8
        response.put("wrongAnswers",   wrong);                        // ← FIX #8
        response.put("submittedAt",    submission.getSubmittedAt());
        response.put("answers",        answerList);                   // ← FIX #8

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // GRADE SUBJECTIVE ANSWER  (Teacher)
    // PUT /submissions/grade/{answerId}
    //
    // FIX #9: Proper typed body (GradeAnswerRequest), sets checkedBy
    // and checkedAt, updates submission.totalScore automatically.
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/grade/{answerId}")
    @Transactional
    public ResponseEntity<String> gradeAnswer(@PathVariable Long answerId,
                                               @RequestBody GradeAnswerRequest request) {

        Optional<Answer> ansOpt = answerRepository.findById(answerId);
        if (!ansOpt.isPresent())
            return ResponseEntity.badRequest().body("Answer not found");

        Answer answer = ansOpt.get();

        // Validate marks against question max
        Optional<Question> qOpt = questionRepository.findById(
            new Question.QuestionId(answer.getTestId(), answer.getQuestionNumber()));

        if (qOpt.isPresent()) {
            int maxMarks = qOpt.get().getMarks();
            if (request.getMarksAwarded().doubleValue() > maxMarks)
                return ResponseEntity.badRequest()
                    .body("Marks awarded (" + request.getMarksAwarded() +
                          ") exceed question max marks (" + maxMarks + ")");
        }

        answer.setMarksAwarded(request.getMarksAwarded());
        answer.setStatus(Answer.AnswerStatus.MANUAL);
        answer.setCheckedBy(request.getCheckedBy());
        answer.setCheckedAt(LocalDateTime.now());
        answerRepository.save(answer);

        // Recalculate and update the submission's total score
        List<Answer> allAnswers = answerRepository.findBySubmissionId(answer.getSubmissionId());
        BigDecimal newTotal = allAnswers.stream()
            .filter(a -> a.getMarksAwarded() != null)
            .map(Answer::getMarksAwarded)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        submissionRepository.findById(answer.getSubmissionId()).ifPresent(sub -> {
            sub.setTotalScore(newTotal);
            // If all subjective answers are now graded, mark as EVALUATED
            boolean allGraded = allAnswers.stream()
                .filter(a -> "SUBJECTIVE".equals(
                    qOpt.map(q -> q.getQuestionType().name()).orElse("")))
                .allMatch(a -> a.getStatus() == Answer.AnswerStatus.MANUAL
                            || a.getStatus() == Answer.AnswerStatus.CHECKED);
            if (allGraded) sub.setStatus(Submission.SubmissionStatus.EVALUATED);
            submissionRepository.save(sub);
        });

        return ResponseEntity.ok("Answer graded. New total score: " + newTotal);
    }

    // ─────────────────────────────────────────────────────────────
    // RESULTS MATRIX
    // GET /submissions/matrix/{testId}
    // Returns summary rows (no full answer detail) — for class leaderboard
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/matrix/{testId}")
    public ResponseEntity<?> matrix(@PathVariable Long testId) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found");

        Test test = testOpt.get();
        List<Submission> submissions = submissionRepository.findByTestId(testId);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Submission s : submissions) {
            User student = userRepository.findById(s.getStudentId()).orElse(null);
            double pct = test.getTotalMarks() > 0
                ? s.getTotalScore().doubleValue() / test.getTotalMarks() * 100.0 : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("submissionId", s.getId());
            row.put("studentId",    s.getStudentId());
            row.put("studentName",  student != null ? student.getName() : s.getStudentId());
            row.put("status",       s.getStatus().name());
            row.put("totalScore",   s.getTotalScore());
            row.put("totalMarks",   test.getTotalMarks());
            row.put("percentage",   Math.round(pct * 10.0) / 10.0);
            row.put("submittedAt",  s.getSubmittedAt());
            rows.add(row);
        }

        return ResponseEntity.ok(rows);
    }
}