package com.jfsd.quize.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jfsd.quize.repository.SubmissionRepository;

@RestController
@RequestMapping("/analytics")
@CrossOrigin
public class AnalyticsController {

    @Autowired private SubmissionRepository submissionRepository;

    // TEST ANALYTICS
    @GetMapping("/test/{testId}")
    public ResponseEntity<?> testAnalytics(@PathVariable Long testId) {
        return ResponseEntity.ok(
                submissionRepository.findByTestId(testId)
        );
    }

    // STUDENT PERFORMANCE
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> studentPerformance(@PathVariable String studentId) {
        return ResponseEntity.ok(
                submissionRepository.findByStudentId(studentId)
        );
    }

    // QUESTION ANALYTICS (basic)
    @GetMapping("/questions/{testId}")
    public ResponseEntity<?> questionAnalytics(@PathVariable Long testId) {
        return ResponseEntity.ok("Question analytics placeholder");
    }
}
