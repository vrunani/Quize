package com.jfsd.quize.repository;

import com.jfsd.quize.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // All answers for a submission
    List<Answer> findBySubmissionId(Long submissionId);

    // Find one specific answer (for save/update)
    Optional<Answer> findBySubmissionIdAndQuestionNumber(Long submissionId, Integer questionNumber);

    // All answers for a test+question (for question-wise analytics)
    List<Answer> findByTestIdAndQuestionNumber(Long testId, Integer questionNumber);
}