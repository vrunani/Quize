package com.jfsd.quize.controller;

import com.jfsd.quize.dto.*;
import com.jfsd.quize.entity.*;
import com.jfsd.quize.entity.Submission.SubmissionStatus;
import com.jfsd.quize.entity.Answer.AnswerStatus;
import com.jfsd.quize.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/student")
@CrossOrigin
public class StudentController {

    @Autowired private TestRepository testRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private AnswerRepository answerRepository;
    @Autowired private NotificationRepository notificationRepository;

    // ─────────────────────────────────────────────────────────────
    // 1. VIEW AVAILABLE TESTS
    // GET /student/tests/available
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/tests/available")
    public ResponseEntity<?> getAvailableTests() {
        LocalDateTime now = LocalDateTime.now();
        List<Test> tests = testRepository
            .findByIsPublishedAndStartTimeBeforeAndEndTimeAfter(true, now, now);
        return ResponseEntity.ok(tests);
    }

    // ─────────────────────────────────────────────────────────────
    // 2. VIEW SCHEDULED TESTS
    // GET /student/tests/scheduled
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/tests/scheduled")
    public ResponseEntity<?> getScheduledTests() {
        LocalDateTime now = LocalDateTime.now();
        List<Test> tests = testRepository
            .findByIsPublishedAndStartTimeAfter(true, now);
        return ResponseEntity.ok(tests);
    }

    // ─────────────────────────────────────────────────────────────
    // 3. START TEST — using examCode
    // POST /student/tests/start
    // Body: { "examCode": "JAVA-MID-01", "studentId": "student@123" }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/tests/start")
    @Transactional
    public ResponseEntity<?> startTest(@RequestBody StartTestRequest request) {

        Optional<Test> testOpt = testRepository.findByExamCode(request.getExamCode());
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with exam code: " + request.getExamCode());

        Test test = testOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (!test.getIsPublished())
            return ResponseEntity.badRequest().body("Test is not published yet");

        if (test.getStartTime() != null && now.isBefore(test.getStartTime()))
            return ResponseEntity.badRequest().body("Test has not started yet. Starts at: " + test.getStartTime());

        if (test.getEndTime() != null && now.isAfter(test.getEndTime()))
            return ResponseEntity.badRequest().body("Test has already ended");

        // Check if already started
        Optional<Submission> existing = submissionRepository
            .findByTestIdAndStudentId(test.getId(), request.getStudentId());

        if (existing.isPresent()) {
            Submission s = existing.get();
            if (s.getStatus() == SubmissionStatus.SUBMITTED || s.getStatus() == SubmissionStatus.EVALUATED)
                return ResponseEntity.badRequest().body("You have already submitted this test");

            // Resume in-progress
            SubmissionResponse response = new SubmissionResponse(
                s.getId(), test.getExamCode(), s.getStudentId(),
                s.getStatus().name(), s.getTotalScore(), s.getSubmittedAt()
            );
            return ResponseEntity.ok(response);
        }

        // Create new submission
        Submission submission = new Submission();
        submission.setTestId(test.getId());
        submission.setStudentId(request.getStudentId());
        submission.setStatus(SubmissionStatus.IN_PROGRESS);
        submission.setTotalScore(BigDecimal.ZERO);
        submissionRepository.save(submission);

        SubmissionResponse response = new SubmissionResponse(
            submission.getId(), test.getExamCode(), submission.getStudentId(),
            submission.getStatus().name(), submission.getTotalScore(), null
        );
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // 4. FETCH TEST QUESTIONS — using examCode
    // GET /student/tests/{examCode}/questions
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/tests/{examCode}/questions")
    public ResponseEntity<?> getTestQuestions(@PathVariable String examCode) {

        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with exam code: " + examCode);

        Test test = testOpt.get();

        if (!test.getIsPublished())
            return ResponseEntity.badRequest().body("Test is not published");

        List<Question> questions = questionRepository
            .findByTestIdOrderByQuestionNumberAsc(test.getId());

        // Hide correct answer from student
        questions.forEach(q -> q.setCorrectOption(null));

        return ResponseEntity.ok(questions);
    }

    // ─────────────────────────────────────────────────────────────
    // 5. SAVE ANSWER / AUTO-SAVE — uses examCode instead of testId
    // POST /student/answers/save
    // Body: { "submissionId":1, "examCode":"JAVA-MID-01", "questionNumber":2, "selectedOption":3 }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/answers/save")
    @Transactional
    public ResponseEntity<String> saveAnswer(@RequestBody SaveAnswerRequest request) {

        Optional<Submission> subOpt = submissionRepository.findById(request.getSubmissionId());
        if (!subOpt.isPresent())
            return ResponseEntity.badRequest().body("Submission not found");

        if (subOpt.get().getStatus() != SubmissionStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().body("Test already submitted");

        Optional<Test> testOpt = testRepository.findByExamCode(request.getExamCode());
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with exam code: " + request.getExamCode());

        Long testId = testOpt.get().getId();

        Optional<Answer> existing = answerRepository
            .findBySubmissionIdAndQuestionNumber(request.getSubmissionId(), request.getQuestionNumber());

        Answer answer = existing.orElse(new Answer());
        answer.setSubmissionId(request.getSubmissionId());
        answer.setTestId(testId);
        answer.setQuestionNumber(request.getQuestionNumber());
        answer.setSelectedOption(request.getSelectedOption());
        answer.setTextAnswer(request.getTextAnswer());

        // Auto-grade objective questions
        if (request.getSelectedOption() != null) {
            Optional<Question> qOpt = questionRepository
                .findById(new Question.QuestionId(testId, request.getQuestionNumber()));

            if (qOpt.isPresent()) {
                Question q = qOpt.get();
                if (q.getCorrectOption() != null &&
                    q.getCorrectOption().equals(request.getSelectedOption())) {
                    answer.setMarksAwarded(BigDecimal.valueOf(q.getMarks()));
                } else {
                    answer.setMarksAwarded(BigDecimal.ZERO);
                }
                answer.setStatus(AnswerStatus.AUTO);
            }
        } else {
            answer.setStatus(AnswerStatus.PENDING);
        }

        answerRepository.save(answer);
        return ResponseEntity.ok("Answer saved successfully");
    }

    // ─────────────────────────────────────────────────────────────
    // 6. SUBMIT TEST — using examCode + studentId
    // PUT /student/tests/{examCode}/submit/{studentId}
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/tests/{examCode}/submit/{studentId}")
    @Transactional
    public ResponseEntity<String> submitTest(@PathVariable String examCode,
                                              @PathVariable String studentId) {

        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with exam code: " + examCode);

        Optional<Submission> subOpt = submissionRepository
            .findByTestIdAndStudentId(testOpt.get().getId(), studentId);

        if (!subOpt.isPresent())
            return ResponseEntity.badRequest().body("No active submission found for this student");

        Submission submission = subOpt.get();

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().body("Test already submitted");

        List<Answer> answers = answerRepository.findBySubmissionId(submission.getId());
        BigDecimal total = answers.stream()
            .filter(a -> a.getMarksAwarded() != null)
            .map(Answer::getMarksAwarded)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setTotalScore(total);
        submissionRepository.save(submission);

        return ResponseEntity.ok("Test submitted successfully. Score (objective only): " + total);
    }

    // ─────────────────────────────────────────────────────────────
    // 7. VIEW SUBMISSION STATUS — using examCode + studentId
    // GET /student/tests/{examCode}/status/{studentId}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/tests/{examCode}/status/{studentId}")
    public ResponseEntity<?> getSubmissionStatus(@PathVariable String examCode,
                                                  @PathVariable String studentId) {

        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with exam code: " + examCode);

        Optional<Submission> subOpt = submissionRepository
            .findByTestIdAndStudentId(testOpt.get().getId(), studentId);

        if (!subOpt.isPresent())
            return ResponseEntity.badRequest().body("No submission found");

        Submission s = subOpt.get();
        SubmissionResponse response = new SubmissionResponse(
            s.getId(), examCode, s.getStudentId(),
            s.getStatus().name(), s.getTotalScore(), s.getSubmittedAt()
        );
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // 8. VIEW RESULTS + REVIEW — using examCode + studentId
    // GET /student/tests/{examCode}/result/{studentId}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/tests/{examCode}/result/{studentId}")
    public ResponseEntity<?> viewResult(@PathVariable String examCode,
                                         @PathVariable String studentId) {

        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with exam code: " + examCode);

        Test test = testOpt.get();

        if (!test.getShowResults())
            return ResponseEntity.badRequest().body("Results are not published yet");

        Optional<Submission> subOpt = submissionRepository
            .findByTestIdAndStudentId(test.getId(), studentId);

        if (!subOpt.isPresent())
            return ResponseEntity.badRequest().body("No submission found");

        Submission submission = subOpt.get();

        if (submission.getStatus() == SubmissionStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().body("You have not submitted this test yet");

        List<Question> questions = questionRepository
            .findByTestIdOrderByQuestionNumberAsc(test.getId());
        List<Answer> answers = answerRepository.findBySubmissionId(submission.getId());

        List<ResultResponse.QuestionResult> questionResults = new ArrayList<>();
        for (Question q : questions) {
            ResultResponse.QuestionResult qr = new ResultResponse.QuestionResult();
            qr.setQuestionNumber(q.getQuestionNumber());
            qr.setQuestionText(q.getQuestionText());
            qr.setQuestionType(q.getQuestionType().name());
            qr.setCorrectOption(q.getCorrectOption());
            qr.setMaxMarks(q.getMarks());

            answers.stream()
                .filter(a -> a.getQuestionNumber().equals(q.getQuestionNumber()))
                .findFirst()
                .ifPresent(a -> {
                    qr.setSelectedOption(a.getSelectedOption());
                    qr.setTextAnswer(a.getTextAnswer());
                    qr.setMarksAwarded(a.getMarksAwarded());
                    qr.setAnswerStatus(a.getStatus().name());
                });

            questionResults.add(qr);
        }

        ResultResponse result = new ResultResponse();
        result.setSubmissionId(submission.getId());
        result.setTestId(test.getId());
        result.setStudentId(studentId);
        result.setTotalScore(submission.getTotalScore());
        result.setTotalMarks(test.getTotalMarks());
        result.setQuestions(questionResults);

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────
    // 9. PERFORMANCE GRAPH
    // GET /student/{studentId}/performance
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/{studentId}/performance")
    public ResponseEntity<?> getPerformance(@PathVariable String studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);

        List<java.util.Map<String, Object>> performance = new ArrayList<>();
        for (Submission s : submissions) {
            if (s.getStatus() == SubmissionStatus.IN_PROGRESS) continue;

            testRepository.findById(s.getTestId()).ifPresent(test -> {
                java.util.Map<String, Object> entry = new java.util.HashMap<>();
                entry.put("examCode", test.getExamCode());
                entry.put("testTitle", test.getTitle());
                entry.put("totalMarks", test.getTotalMarks());
                entry.put("scoredMarks", s.getTotalScore());
                entry.put("submittedAt", s.getSubmittedAt());
                entry.put("status", s.getStatus().name());
                performance.add(entry);
            });
        }

        return ResponseEntity.ok(performance);
    }

    // ─────────────────────────────────────────────────────────────
    // 10. GET NOTIFICATIONS
    // GET /student/{userId}/notifications
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/{userId}/notifications")
    public ResponseEntity<?> getNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(
            notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 11. MARK NOTIFICATION AS READ
    // PUT /student/notifications/{notificationId}/read
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/notifications/{notificationId}/read")
    @Transactional
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
        if (!notifOpt.isPresent())
            return ResponseEntity.badRequest().body("Notification not found");

        Notification n = notifOpt.get();
        n.setIsRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok("Marked as read");
    }
}