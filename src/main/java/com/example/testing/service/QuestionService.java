package com.example.testing.service;

import com.example.testing.entity.Question;
import com.example.testing.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис работы с вопросами теста. Используется в основном для подгрузки
 * вопросов при старте попытки — отдельно от агрегата {@code Test}, чтобы
 * не завязываться на lazy-инициализацию JPA-коллекции вне транзакции.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<Question> findByTestId(Long testId) {
        return questionRepository.findByTestIdOrderByIdAsc(testId);
    }

    @Transactional(readOnly = true)
    public long countByTestId(Long testId) {
        return questionRepository.countByTestId(testId);
    }
}
