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
    @Autowired private TestRepository     testRepository;

    // ─────────────────────────────────────────────────────────────
    // ADD QUESTION
    // POST /questions/add
    // Body: { examCode, questionText, questionType, option1-4, correctOption, marks }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> addQuestion(@RequestBody QuestionRequest req) {
        Optional<Test> testOpt = testRepository.findByExamCode(req.getExamCode());
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found: " + req.getExamCode());

        Long testId = testOpt.get().getId();
        Integer next = questionRepository.findMaxQuestionNumberByTestId(testId) + 1;

        Question q = mapToQuestion(new Question(), req, testId, next);
        questionRepository.save(q);
        return ResponseEntity.ok("Question " + next + " added to exam: " + req.getExamCode());
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE QUESTION
    // PUT /questions/update/{examCode}/{questionNumber}
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/update/{examCode}/{questionNumber}")
    @Transactional
    public ResponseEntity<String> updateQuestion(@PathVariable String examCode,
                                                  @PathVariable Integer questionNumber,
                                                  @RequestBody QuestionRequest req) {
        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found: " + examCode);

        Long testId = testOpt.get().getId();
        Optional<Question> optional = questionRepository.findById(new QuestionId(testId, questionNumber));
        if (!optional.isPresent())
            return ResponseEntity.badRequest().body("Question not found: " + questionNumber);

        Question q = optional.get();
        if (req.getQuestionText()  != null)  q.setQuestionText(req.getQuestionText());
        if (req.getQuestionType()  != null)  q.setQuestionType(req.getQuestionType());
        if (req.getOption1()       != null)  q.setOption1(req.getOption1());
        if (req.getOption2()       != null)  q.setOption2(req.getOption2());
        if (req.getOption3()       != null)  q.setOption3(req.getOption3());
        if (req.getOption4()       != null)  q.setOption4(req.getOption4());
        if (req.getCorrectOption() != null)  q.setCorrectOption(req.getCorrectOption());
        if (req.getMarks()         >  0)     q.setMarks(req.getMarks());

        questionRepository.save(q);
        return ResponseEntity.ok("Question " + questionNumber + " of '" + examCode + "' updated");
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE QUESTION + auto re-number
    // DELETE /questions/delete/{examCode}/{questionNumber}
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/delete/{examCode}/{questionNumber}")
    @Transactional
    public ResponseEntity<String> deleteQuestion(@PathVariable String examCode,
                                                  @PathVariable Integer questionNumber) {
        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found: " + examCode);

        Long testId = testOpt.get().getId();
        QuestionId qid = new QuestionId(testId, questionNumber);

        if (!questionRepository.existsById(qid))
            return ResponseEntity.badRequest().body("Question not found: " + questionNumber);

        questionRepository.deleteById(qid);
        questionRepository.flush();

        // Shift all higher-numbered questions down by 1
        List<Question> after = questionRepository
            .findByTestIdAndQuestionNumberGreaterThanOrderByQuestionNumberAsc(testId, questionNumber);
        questionRepository.deleteAll(after);
        questionRepository.flush();
        for (Question q : after) {
            q.setQuestionNumber(q.getQuestionNumber() - 1);
            questionRepository.save(q);
        }

        return ResponseEntity.ok("Question " + questionNumber + " deleted. Remaining re-numbered.");
    }

    // ─────────────────────────────────────────────────────────────
    // GET BY EXAM CODE  (Teacher view — includes correct answers)
    // GET /questions/exam/{examCode}
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/exam/{examCode}")
    public ResponseEntity<?> getByExamCode(@PathVariable String examCode) {
        Optional<Test> testOpt = testRepository.findByExamCode(examCode);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found: " + examCode);

        return ResponseEntity.ok(
            questionRepository.findByTestIdOrderByQuestionNumberAsc(testOpt.get().getId()));
    }

    // ─────────────────────────────────────────────────────────────
    // GET BY TEST ID  (Student exam — hides correct answers)
    // GET /questions/test/{testId}
    //
    // NOTE: This is the endpoint student.html calls after verifying
    // the exam code via GET /tests/find/{examCode} which returns testId.
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/test/{testId}")
    public ResponseEntity<?> getByTestId(@PathVariable Long testId) {
        Optional<Test> testOpt = testRepository.findById(testId);
        if (!testOpt.isPresent())
            return ResponseEntity.badRequest().body("Test not found");

        Test test = testOpt.get();
        if (!Boolean.TRUE.equals(test.getIsPublished()))
            return ResponseEntity.badRequest().body("Test is not published");

        List<Question> questions =
            questionRepository.findByTestIdOrderByQuestionNumberAsc(testId);

        // Hide correct answers from student
        questions.forEach(q -> q.setCorrectOption(null));

        return ResponseEntity.ok(questions);
    }

    // ─────────────────────────────────────────────────────────────
    // private helper
    // ─────────────────────────────────────────────────────────────
    private Question mapToQuestion(Question q, QuestionRequest req,
                                    Long testId, Integer number) {
        q.setTestId(testId);
        q.setQuestionNumber(number);
        q.setQuestionText(req.getQuestionText());
        q.setQuestionType(req.getQuestionType());
        q.setOption1(req.getOption1());
        q.setOption2(req.getOption2());
        q.setOption3(req.getOption3());
        q.setOption4(req.getOption4());
        q.setCorrectOption(req.getCorrectOption());
        q.setMarks(req.getMarks());
        return q;
    }
}