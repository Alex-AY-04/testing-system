package com.example.testing.service;

import com.example.testing.entity.Attempt;
import com.example.testing.entity.Question;
import com.example.testing.entity.Test;
import com.example.testing.repository.AttemptRepository;
import com.example.testing.session.TestSessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты сервиса прохождения попыток.
 * Проверяется корректность подсчёта правильных ответов и выставления оценки,
 * а также то, что при старте теста перемешиваются варианты и создаётся
 * корректное состояние сессии.
 */
@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock private AttemptRepository attemptRepository;
    @Mock private QuestionService questionService;
    @Mock private TestService testService;

    // Реальные (не мок) реализации — это простые детерминированные сервисы без I/O
    private final AnswerShuffleService shuffleService = new AnswerShuffleService();
    private final GradingService gradingService = new GradingService();

    private AttemptService service;

    @BeforeEach
    void init() {
        service = new AttemptService(attemptRepository, questionService, testService,
                shuffleService, gradingService);
    }

    private Question question(Long id, String correct) {
        Question q = new Question();
        q.setId(id);
        q.setWording("wording-" + id);
        q.setCorrectAns(correct);
        q.setIncorrectAns1("wrong1-" + id);
        q.setIncorrectAns2("wrong2-" + id);
        q.setIncorrectAns3("wrong3-" + id);
        return q;
    }

    /**
     * При старте теста состояние сессии должно содержать все вопросы
     * с перемешанными вариантами ответа из 4 элементов и незаполненными
     * выбранными ответами.
     */
    @org.junit.jupiter.api.Test
    void startAttemptBuildsSessionStateWithShuffledOptions() {
        Test test = new Test();
        test.setId(1L);
        test.setName("Sample");

        when(testService.findById(1L)).thenReturn(Optional.of(test));
        when(questionService.findByTestId(1L)).thenReturn(List.of(
                question(10L, "A"),
                question(11L, "B")
        ));

        TestSessionState state = service.startAttempt(1L);

        assertEquals(1L, state.getTestId());
        assertNotNull(state.getStartTime());
        assertEquals(2, state.getQuestions().size());
        for (TestSessionState.QuestionEntry e : state.getQuestions()) {
            assertEquals(4, e.getShuffledOptions().size());
            assertEquals(null, e.getSelectedAnswer());
        }
    }

    /**
     * Корректный подсчёт правильных ответов: если студент выбрал
     * правильный текст — учитывается; если неверный или null — нет.
     */
    @org.junit.jupiter.api.Test
    void countsCorrectAnswersProperly() {
        TestSessionState state = new TestSessionState();
        state.setQuestions(List.of(
                entry("A", "A"),   // верно
                entry("B", "X"),   // неверно
                entry("C", null),  // не отвечено
                entry("D", "D")    // верно
        ));
        assertEquals(2, service.countCorrect(state));
    }

    /**
     * Завершение попытки должно выставить правильную оценку и сохранить
     * запись в БД с корректными полями (имя студента, число правильных,
     * оценка, время начала/конца).
     */
    @org.junit.jupiter.api.Test
    void finishAttemptSavesCorrectAttemptRow() {
        Test test = new Test();
        test.setId(7L);
        test.setName("Final test");

        when(testService.findById(7L)).thenReturn(Optional.of(test));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(inv -> inv.getArgument(0));

        TestSessionState state = new TestSessionState();
        state.setTestId(7L);
        state.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        // 4 из 5 правильных — ratio 0.80 → оценка 5
        state.setQuestions(List.of(
                entry("A", "A"),
                entry("B", "B"),
                entry("C", "C"),
                entry("D", "D"),
                entry("E", "X")
        ));

        Attempt result = service.finishAttempt(state, "Иван Петров");

        ArgumentCaptor<Attempt> captor = ArgumentCaptor.forClass(Attempt.class);
        verify(attemptRepository).save(captor.capture());
        Attempt saved = captor.getValue();
        assertEquals("Иван Петров", saved.getName());
        assertEquals(4, saved.getCorrectAns());
        assertEquals((short) 5, saved.getGrade());
        assertEquals(state.getStartTime(), saved.getStartTime());
        assertNotNull(saved.getEndTime());
        assertEquals(result, saved);
    }

    private TestSessionState.QuestionEntry entry(String correct, String selected) {
        TestSessionState.QuestionEntry e = new TestSessionState.QuestionEntry();
        e.setCorrectAnswer(correct);
        e.setSelectedAnswer(selected);
        return e;
    }
}
