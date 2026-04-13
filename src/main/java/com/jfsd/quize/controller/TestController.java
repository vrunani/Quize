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
            return ResponseEntity.badRequest().body("Test with examCode '"
                + request.getExamCode() + "' already exists");

        Test test = new Test();
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setExamCode(request.getExamCode());
        test.setCreatedBy(request.getCreatedBy());
        test.setTotalMarks(request.getTotalMarks() != null ? request.getTotalMarks() : 0);
        test.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 0);
        test.setStartTime(request.getStartTime());
        test.setEndTime(request.getEndTime());
        test.setNegativeMarking(
            request.getNegativeMarking() != null ? request.getNegativeMarking() : BigDecimal.ZERO);
        test.setIsPublished(
            request.getIsPublished() != null ? request.getIsPublished() : false);
        test.setShowResults(
            request.getShowResults() != null ? request.getShowResults() : false);

        testRepository.save(test);
        return ResponseEntity.ok("Test added successfully with examCode: " + test.getExamCode());
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE TEST (by examCode — no internal ID needed)
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
        // Note: examCode itself is intentionally NOT updatable here to keep it stable as the key

        testRepository.save(test);
        return ResponseEntity.ok("Test updated successfully for examCode: " + examCode);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE TEST (by examCode — no internal ID needed)
    // DELETE /tests/delete/{examCode}
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/delete/{examCode}")
    @Transactional
    public ResponseEntity<String> deleteTest(@PathVariable String examCode) {
        Optional<Test> optional = testRepository.findByExamCode(examCode);
        if (!optional.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        Long testId = optional.get().getId();

        // Delete all questions for this test first (avoid FK constraint)
        questionRepository.deleteAllByTestId(testId);

        testRepository.deleteById(testId);
        return ResponseEntity.ok("Test deleted successfully for examCode: " + examCode);
    }

    // ─────────────────────────────────────────────────────────────
    // GET TEST BY EXAM CODE
    // GET /tests/find/{examCode}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/find/{examCode}")
    public ResponseEntity<?> getTest(@PathVariable String examCode) {
        Optional<Test> optional = testRepository.findByExamCode(examCode);
        if (!optional.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        return ResponseEntity.ok(optional.get());
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL TESTS
    // GET /tests/all
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<?> getAllTests() {
        return ResponseEntity.ok(testRepository.findAll());
    }
}
