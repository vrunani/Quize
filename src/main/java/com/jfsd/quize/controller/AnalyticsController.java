package com.jfsd.quize.controller;

import com.jfsd.quize.dto.StudentPerformanceResponse;
import com.jfsd.quize.entity.Answer;
import com.jfsd.quize.entity.Question;
import com.jfsd.quize.entity.Submission;
import com.jfsd.quize.entity.Test;
import com.jfsd.quize.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/analytics")
@CrossOrigin
public class AnalyticsController {

    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private TestRepository       testRepository;
    @Autowired private QuestionRepository   questionRepository;
    @Autowired private AnswerRepository     answerRepository;

    // ─────────────────────────────────────────────────────────────
    // TEACHER — TEST ANALYTICS DASHBOARD
    // GET /analytics/test/{testId}
    // Returns: average score, pass rate, score distribution, top performers
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/test/{testId}")
    public ResponseEntity<?> testAnalytics(@PathVariable Long testId) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found");

        Test test = testOpt.get();
        List<Submission> submissions = submissionRepository.findByTestId(testId);

        long submitted = submissions.stream()
            .filter(s -> s.getStatus() != Submission.SubmissionStatus.IN_PROGRESS)
            .count();

        double avgScore = submissions.stream()
            .filter(s -> s.getStatus() != Submission.SubmissionStatus.IN_PROGRESS)
            .mapToDouble(s -> s.getTotalScore().doubleValue())
            .average().orElse(0.0);

        double avgPct = test.getTotalMarks() > 0 ? avgScore / test.getTotalMarks() * 100.0 : 0.0;

        long passed = submissions.stream()
            .filter(s -> s.getStatus() != Submission.SubmissionStatus.IN_PROGRESS)
            .filter(s -> test.getTotalMarks() > 0 &&
                    s.getTotalScore().doubleValue() / test.getTotalMarks() >= 0.40)
            .count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testId",           test.getId());
        response.put("testTitle",         test.getTitle());
        response.put("totalMarks",        test.getTotalMarks());
        response.put("totalSubmissions",  submitted);
        response.put("averageScore",      Math.round(avgScore * 100.0) / 100.0);
        response.put("averagePercentage", Math.round(avgPct  * 10.0)  / 10.0);
        response.put("passCount",         passed);
        response.put("failCount",         submitted - passed);
        response.put("passRate",          submitted > 0
            ? Math.round((double) passed / submitted * 100.0 * 10.0) / 10.0 : 0.0);

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // STUDENT — PERFORMANCE GRAPH
    // GET /analytics/student/{studentId}
    //
    // FIX #1: Returns List<StudentPerformanceResponse> with the
    // fields the graph in student.html actually reads:
    //   testTitle, percentage, marksObtained, totalMarks
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> studentPerformance(@PathVariable String studentId) {

        List<Submission> submissions = submissionRepository.findByStudentId(studentId);

        List<StudentPerformanceResponse> result = new ArrayList<>();
        for (Submission s : submissions) {
            if (s.getStatus() == Submission.SubmissionStatus.IN_PROGRESS) continue;

            testRepository.findById(s.getTestId()).ifPresent(test -> {
                // Only include tests where results are published
                if (Boolean.TRUE.equals(test.getShowResults())) {
                    result.add(new StudentPerformanceResponse(
                        test.getId(),
                        test.getExamCode(),
                        test.getTitle(),
                        test.getTotalMarks(),
                        s.getTotalScore(),
                        s.getStatus().name(),
                        s.getSubmittedAt()
                    ));
                }
            });
        }

        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────
    // QUESTION-WISE ANALYTICS (Teacher)
    // GET /analytics/questions/{testId}
    // Returns per-question: attempts, correct count, wrong count, avg marks
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/questions/{testId}")
    public ResponseEntity<?> questionAnalytics(@PathVariable Long testId) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found");

        List<Question> questions = questionRepository.findByTestIdOrderByQuestionNumberAsc(testId);
        List<Map<String, Object>> analytics = new ArrayList<>();

        for (Question q : questions) {
            List<Answer> answers =
                answerRepository.findByTestIdAndQuestionNumber(testId, q.getQuestionNumber());

            long attempted = answers.stream()
                .filter(a -> a.getSelectedOption() != null || a.getTextAnswer() != null)
                .count();

            long correct = 0;
            if (q.getQuestionType() == Question.QuestionType.OBJECTIVE) {
                correct = answers.stream()
                    .filter(a -> a.getSelectedOption() != null &&
                                 a.getSelectedOption().equals(q.getCorrectOption()))
                    .count();
            }

            double avgMarks = answers.stream()
                .filter(a -> a.getMarksAwarded() != null)
                .mapToDouble(a -> a.getMarksAwarded().doubleValue())
                .average().orElse(0.0);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionNumber", q.getQuestionNumber());
            row.put("questionText",   q.getQuestionText().length() > 80
                ? q.getQuestionText().substring(0, 80) + "…" : q.getQuestionText());
            row.put("questionType",   q.getQuestionType().name());
            row.put("maxMarks",       q.getMarks());
            row.put("attempted",      attempted);
            row.put("correct",        correct);
            row.put("wrong",          q.getQuestionType() == Question.QuestionType.OBJECTIVE
                ? attempted - correct : 0);
            row.put("avgMarks",       Math.round(avgMarks * 100.0) / 100.0);
            analytics.add(row);
        }

        return ResponseEntity.ok(analytics);
    }
}