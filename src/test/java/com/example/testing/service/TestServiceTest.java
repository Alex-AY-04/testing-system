package com.example.testing.service;

import com.example.testing.dto.QuestionFormDto;
import com.example.testing.dto.TestFormDto;
import com.example.testing.entity.Test;
import com.example.testing.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты сервиса управления тестами.
 * Используется Mockito для мокирования {@link TestRepository},
 * чтобы изолированно проверить логику валидации и маппинга DTO в сущности.
 */
@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private TestService service;

    private QuestionFormDto fullQuestion(String prefix) {
        return new QuestionFormDto(null, prefix + " wording",
                prefix + " correct", prefix + " i1", prefix + " i2", prefix + " i3");
    }

    @BeforeEach
    void setUp() {
        when(testRepository.save(any(Test.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * Штатное создание теста с одним полностью заполненным вопросом.
     * Проверяем, что save() вызывается и в сохранённой сущности ровно 1 вопрос.
     */
    @org.junit.jupiter.api.Test
    void createsTestWithOneQuestion() {
        TestFormDto form = new TestFormDto();
        form.setName("Мой тест");
        form.setQuestions(List.of(fullQuestion("q1")));

        service.save(form);

        ArgumentCaptor<Test> captor = ArgumentCaptor.forClass(Test.class);
        verify(testRepository).save(captor.capture());
        Test saved = captor.getValue();
        assertEquals("Мой тест", saved.getName());
        assertEquals(1, saved.getQuestions().size());
    }

    /**
     * Тест без названия (пустая строка) должен отклоняться с IllegalArgumentException,
     * save() вызываться не должен.
     */
    @org.junit.jupiter.api.Test
    void rejectsTestWithoutName() {
        TestFormDto form = new TestFormDto();
        form.setName("   ");
        form.setQuestions(List.of(fullQuestion("q1")));

        assertThrows(IllegalArgumentException.class, () -> service.save(form));
        verify(testRepository, never()).save(any());
    }

    /**
     * Тест без вопросов вообще — должен отклоняться.
     */
    @org.junit.jupiter.api.Test
    void rejectsTestWithoutQuestions() {
        TestFormDto form = new TestFormDto();
        form.setName("Пустой тест");

        assertThrows(IllegalArgumentException.class, () -> service.save(form));
        verify(testRepository, never()).save(any());
    }

    /**
     * Тест, в котором все вопросы частично заполнены, должен отклоняться:
     * ни один не проходит проверку isFullyFilled.
     */
    @org.junit.jupiter.api.Test
    void rejectsTestWhereAllQuestionsPartiallyFilled() {
        TestFormDto form = new TestFormDto();
        form.setName("Тест");
        QuestionFormDto partial = new QuestionFormDto(null, "wording", "correct", null, "i2", "i3");
        form.setQuestions(List.of(partial));

        assertThrows(IllegalArgumentException.class, () -> service.save(form));
        verify(testRepository, never()).save(any());
    }

    /**
     * Частично заполненные вопросы должны отбрасываться, а полностью
     * заполненные — сохраняться. Из трёх вопросов (2 частичных + 1 полный)
     * в сущности должен остаться ровно 1.
     */
    @org.junit.jupiter.api.Test
    void dropsPartiallyFilledQuestionsButKeepsFull() {
        TestFormDto form = new TestFormDto();
        form.setName("Тест");
        QuestionFormDto partial1 = new QuestionFormDto(null, "w", null, null, null, null);
        QuestionFormDto partial2 = new QuestionFormDto(null, "w", "c", "i1", null, null);
        QuestionFormDto full = fullQuestion("full");
        form.setQuestions(List.of(partial1, partial2, full));

        service.save(form);

        ArgumentCaptor<Test> captor = ArgumentCaptor.forClass(Test.class);
        verify(testRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getQuestions().size());
    }
}
