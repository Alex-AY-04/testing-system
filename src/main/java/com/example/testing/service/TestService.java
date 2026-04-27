package com.example.testing.service;

import com.example.testing.dto.QuestionFormDto;
import com.example.testing.dto.TestFormDto;
import com.example.testing.entity.Question;
import com.example.testing.entity.Test;
import com.example.testing.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис управления тестами: создание, редактирование, удаление.
 * <p>
 * Валидация:
 * <ul>
 *     <li>название теста не должно быть пустым;</li>
 *     <li>в тесте должен остаться хотя бы один полностью заполненный вопрос
 *         после отбрасывания частично заполненных и пустых блоков.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

    @Transactional(readOnly = true)
    public List<Test> findAll() {
        return testRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Test> findById(Long id) {
        return testRepository.findById(id);
    }

    @Transactional
    public Test save(TestFormDto form) {
        validateForm(form);

        Test test = (form.getId() != null)
                ? testRepository.findById(form.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Test not found: " + form.getId()))
                : new Test();

        test.setName(form.getName().trim());
        // полностью заменяем список вопросов — orphanRemoval удалит старые
        test.getQuestions().clear();

        for (QuestionFormDto q : form.getQuestions()) {
            if (q == null || !q.isFullyFilled()) {
                continue;
            }
            Question entity = Question.builder()
                    .test(test)
                    .wording(q.getWording().trim())
                    .correctAns(q.getCorrectAns().trim())
                    .incorrectAns1(q.getIncorrectAns1().trim())
                    .incorrectAns2(q.getIncorrectAns2().trim())
                    .incorrectAns3(q.getIncorrectAns3().trim())
                    .build();
            test.getQuestions().add(entity);
        }

        return testRepository.save(test);
    }

    @Transactional
    public void delete(Long id) {
        testRepository.deleteById(id);
    }

    private void validateForm(TestFormDto form) {
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название теста не может быть пустым");
        }
        if (form.getQuestions() == null
                || form.getQuestions().stream().noneMatch(q -> q != null && q.isFullyFilled())) {
            throw new IllegalArgumentException("Тест должен содержать хотя бы один полностью заполненный вопрос");
        }
    }
}
