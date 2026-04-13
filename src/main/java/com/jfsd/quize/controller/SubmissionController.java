package com.jfsd.quize.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jfsd.quize.entity.Answer;
import com.jfsd.quize.repository.AnswerRepository;
import com.jfsd.quize.repository.SubmissionRepository;
@RestController
@RequestMapping("/submissions")
@CrossOrigin
public class SubmissionController {

    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private AnswerRepository answerRepository;

    // ALL SUBMISSIONS FOR TEST
    @GetMapping("/test/{testId}")
    public ResponseEntity<?> getSubmissions(@PathVariable Long testId) {
        return ResponseEntity.ok(
                submissionRepository.findByTestId(testId)
        );
    }

    // GRADE SUBJECTIVE
    @PutMapping("/grade/{answerId}")
    public ResponseEntity<?> grade(@PathVariable Long answerId, @RequestBody Map<String, Object> body) {

        Answer answer = answerRepository.findById(answerId).get();
        answer.setMarksAwarded(new BigDecimal(body.get("marksAwarded").toString()));
        answer.setStatus(Answer.AnswerStatus.MANUAL);

        answerRepository.save(answer);
        return ResponseEntity.ok("Graded");
    }

    // RESULT MATRIX
    @GetMapping("/matrix/{testId}")
    public ResponseEntity<?> matrix(@PathVariable Long testId) {
        return ResponseEntity.ok(
                submissionRepository.findByTestId(testId)
        );
    }
}

