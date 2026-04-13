package com.jfsd.quize.repository;

import com.jfsd.quize.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Check if student already started this test
    Optional<Submission> findByTestIdAndStudentId(Long testId, String studentId);

    // All submissions by a student (for performance graph)
    List<Submission> findByStudentId(String studentId);

    // All submissions for a test (teacher review)
    List<Submission> findByTestId(Long testId);
}