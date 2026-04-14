package com.jfsd.quize.controller;

import com.jfsd.quize.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jfsd.quize.dto.AddTestRequest;
import com.jfsd.quize.entity.Test;
import com.jfsd.quize.repository.TestRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tests")
@CrossOrigin
public class TestController {

    @Autowired private TestRepository testRepository;
    @Autowired private QuestionRepository questionRepository;

    // ─────────────────────────────────────────────────────────────
    // ADD TEST
    // POST /tests/add
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<String> addTest(@RequestBody AddTestRequest request) {
        if (request.getExamCode() == null || request.getExamCode().isBlank())
            return ResponseEntity.badRequest().body("examCode is required");

        if (testRepository.findByExamCode(request.getExamCode()).isPresent())
            return ResponseEntity.badRequest().body(
                "Test with examCode '" + request.getExamCode() + "' already exists");

        Test test = buildTest(new Test(), request);
        testRepository.save(test);
        return ResponseEntity.ok("Test added successfully with examCode: " + test.getExamCode());
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE TEST
    // PUT /tests/update/{examCode}
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/update/{examCode}")
    public ResponseEntity<String> updateTest(@PathVariable String examCode,
                                              @RequestBody AddTestRequest request) {
        Optional<Test> optional = testRepository.findByExamCode(examCode);
        if (!optional.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        Test test = optional.get();
        if (request.getTitle() != null)           test.setTitle(request.getTitle());
        if (request.getDescription() != null)     test.setDescription(request.getDescription());
        if (request.getCreatedBy() != null)       test.setCreatedBy(request.getCreatedBy());
        if (request.getTotalMarks() != null)      test.setTotalMarks(request.getTotalMarks());
        if (request.getDurationMinutes() != null) test.setDurationMinutes(request.getDurationMinutes());
        if (request.getStartTime() != null)       test.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)         test.setEndTime(request.getEndTime());
        if (request.getNegativeMarking() != null) test.setNegativeMarking(request.getNegativeMarking());
        if (request.getIsPublished() != null)     test.setIsPublished(request.getIsPublished());
        if (request.getShowResults() != null)     test.setShowResults(request.getShowResults());

        testRepository.save(test);
        return ResponseEntity.ok("Test updated successfully for examCode: " + examCode);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE TEST
    // DELETE /tests/delete/{examCode}
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/delete/{examCode}")
    @Transactional
    public ResponseEntity<String> deleteTest(@PathVariable String examCode) {
        Optional<Test> optional = testRepository.findByExamCode(examCode);
        if (!optional.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        Long testId = optional.get().getId();
        questionRepository.deleteAllByTestId(testId);
        testRepository.deleteById(testId);
        return ResponseEntity.ok("Test deleted successfully for examCode: " + examCode);
    }

    // ─────────────────────────────────────────────────────────────
    // GET BY EXAM CODE
    // GET /tests/find/{examCode}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/find/{examCode}")
    public ResponseEntity<?> getTest(@PathVariable String examCode) {
        return testRepository.findByExamCode(examCode)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Test not found with examCode: " + examCode));
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL TESTS (draft + published)
    // GET /tests/all
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<?> getAllTests() {
        return ResponseEntity.ok(testRepository.findAll());
    }

    // ─────────────────────────────────────────────────────────────
    // GET PUBLISHED TESTS  (FIX #3 — was missing, student dashboard needs it)
    // GET /tests/published
    // Returns all tests where isPublished=true (includes upcoming + active)
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedTests() {
        List<Test> tests = testRepository.findByIsPublished(true);
        return ResponseEntity.ok(tests);
    }

    // ─────────────────────────────────────────────────────────────
    // GET ONGOING TESTS
    // GET /tests/ongoing
    // isPublished=true AND startTime <= now <= endTime
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/ongoing")
    public ResponseEntity<?> getOngoingTests() {
        LocalDateTime now = LocalDateTime.now();
        List<Test> tests = testRepository
            .findByIsPublishedAndStartTimeBeforeAndEndTimeAfter(true, now, now);
        return ResponseEntity.ok(tests);
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMPLETED TESTS  (FIX #3 — needed for student review filter)
    // GET /tests/completed
    // isPublished=true AND endTime < now
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedTests() {
        LocalDateTime now = LocalDateTime.now();
        List<Test> tests = testRepository.findByEndTimeBefore(now);
        return ResponseEntity.ok(tests);
    }

    // ─────────────────────────────────────────────────────────────
    // private helper — avoids duplicate mapping logic
    // ─────────────────────────────────────────────────────────────
    private Test buildTest(Test test, AddTestRequest r) {
        test.setTitle(r.getTitle());
        test.setDescription(r.getDescription());
        test.setExamCode(r.getExamCode());
        test.setCreatedBy(r.getCreatedBy());
        test.setTotalMarks(r.getTotalMarks()      != null ? r.getTotalMarks()      : 0);
        test.setDurationMinutes(r.getDurationMinutes() != null ? r.getDurationMinutes() : 0);
        test.setStartTime(r.getStartTime());
        test.setEndTime(r.getEndTime());
        test.setNegativeMarking(r.getNegativeMarking() != null ? r.getNegativeMarking() : BigDecimal.ZERO);
        test.setIsPublished(r.getIsPublished()     != null ? r.getIsPublished()     : false);
        test.setShowResults(r.getShowResults()     != null ? r.getShowResults()     : false);
        return test;
    }
}