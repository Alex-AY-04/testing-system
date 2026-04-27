package com.example.testing.repository;

import com.example.testing.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTestIdOrderByIdAsc(Long testId);

    long countByTestId(Long testId);
}
