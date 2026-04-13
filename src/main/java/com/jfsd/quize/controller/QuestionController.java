package com.jfsd.quize.controller;

import com.jfsd.quize.dto.QuestionRequest;
import com.jfsd.quize.entity.Question;
import com.jfsd.quize.entity.Question.QuestionId;
import com.jfsd.quize.entity.Test;
import com.jfsd.quize.repository.QuestionRepository;
import com.jfsd.quize.repository.TestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/questions")
@CrossOrigin
public class QuestionController {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private TestRepository testRepository;

    @GetMapping("/ping")
    public String ping() { return "Server is working!"; }

    // ─────────────────────────────────────────────────────────────
    // ADD QUESTION
    // POST /questions/add
    // Body: { "examCode": "JAVA-MID-01", "questionText": "...", ... }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> addQuestion(@RequestBody QuestionRequest req) {
        try {
            // Resolve examCode → testId
            Optional<Test> testOpt = testRepository.findByExamCode(req.getExamCode());
            if (!testOpt.isPresent())
                return ResponseEntity.badRequest().body("Test not found with examCode: " + req.getExamCode());

            Long testId = testOpt.get().getId();

            Integer nextQuestionNumber =
                questionRepository.findMaxQuestionNumberByTestId(testId) + 1;

            Question q = new Question();
            q.setTestId(testId);
            q.setQuestionNumber(nextQuestionNumber);
            q.setQuestionText(req.getQuestionText());
            q.setQuestionType(req.getQuestionType());
            q.setOption1(req.getOption1());
            q.setOption2(req.getOption2());
            q.setOption3(req.getOption3());
            q.setOption4(req.getOption4());
            q.setCorrectOption(req.getCorrectOption());
            q.setMarks(req.getMarks());

            questionRepository.save(q);
            return ResponseEntity.ok("Question added with number: " + nextQuestionNumber
                + " to exam: " + req.getExamCode());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE QUESTION
    // PUT /questions/update/{examCode}/{questionNumber}
    // Body: { "questionText": "...", ... }  (examCode in path, not body)
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/update/{examCode}/{questionNumber}")
    @Transactional
    public ResponseEntity<String> updateQuestion(@PathVariable String examCode,@PathVariable Integer questionNumber,
                                                @RequestBody QuestionRequest req) {
        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        Long testId = testOpt.get().getId();
        QuestionId qid = new QuestionId(testId, questionNumber);
        Optional<Question> optional = questionRepository.findById(qid);

        if (!optional.isPresent())
            return ResponseEntity.badRequest().body(
                "Question not found: examCode=" + examCode + ", questionNumber=" + questionNumber);

        Question q = optional.get();
        if (req.getQuestionText() != null)  q.setQuestionText(req.getQuestionText());
        if (req.getQuestionType() != null)  q.setQuestionType(req.getQuestionType());
        if (req.getOption1() != null)       q.setOption1(req.getOption1());
        if (req.getOption2() != null)       q.setOption2(req.getOption2());
        if (req.getOption3() != null)       q.setOption3(req.getOption3());
        if (req.getOption4() != null)       q.setOption4(req.getOption4());
        if (req.getCorrectOption() != null) q.setCorrectOption(req.getCorrectOption());
        if (req.getMarks() > 0)             q.setMarks(req.getMarks());

        questionRepository.save(q);
        return ResponseEntity.ok("Question " + questionNumber + " of exam '"
            + examCode + "' updated successfully");
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE QUESTION + AUTO RE-NUMBER
    // DELETE /questions/delete/{examCode}/{questionNumber}
    //
    // Example: exam has Q1, Q2, Q3, Q4
    //   Delete Q2 → Q3 becomes Q2, Q4 becomes Q3
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/delete/{examCode}/{questionNumber}")
    @Transactional
    public ResponseEntity<String> deleteQuestion(@PathVariable String examCode,
                                                  @PathVariable Integer questionNumber) {
        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        Long testId = testOpt.get().getId();
        QuestionId qid = new QuestionId(testId, questionNumber);

        if (!questionRepository.existsById(qid))
            return ResponseEntity.badRequest().body(
                "Question not found: examCode=" + examCode + ", questionNumber=" + questionNumber);

        // Step 1: Delete the target question
        questionRepository.deleteById(qid);
        questionRepository.flush();

        // Step 2: Fetch all questions after deleted one (higher question numbers)
        List<Question> questionsAfter = questionRepository
            .findByTestIdAndQuestionNumberGreaterThanOrderByQuestionNumberAsc(testId, questionNumber);

        // Step 3: Delete those from DB (required because questionNumber is part of composite PK)
        questionRepository.deleteAll(questionsAfter);
        questionRepository.flush();

        // Step 4: Re-save each with questionNumber - 1  (shift down by 1)
        for (Question q : questionsAfter) {
            q.setQuestionNumber(q.getQuestionNumber() - 1);
            questionRepository.save(q);
        }

        return ResponseEntity.ok("Question " + questionNumber + " deleted from exam '"
            + examCode + "'. Remaining questions re-numbered.");
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL QUESTIONS FOR A TEST
    // GET /questions/exam/{examCode}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/exam/{examCode}")
    public ResponseEntity<?> getQuestionsByExamCode(@PathVariable String examCode) {
        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found with examCode: " + examCode);

        List<Question> questions = questionRepository
            .findByTestIdOrderByQuestionNumberAsc(testOpt.get().getId());
        return ResponseEntity.ok(questions);
    }
}
