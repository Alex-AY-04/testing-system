package com.example.testing.service;

import com.example.testing.entity.Attempt;
import com.example.testing.entity.Question;
import com.example.testing.entity.Test;
import com.example.testing.repository.AttemptRepository;
import com.example.testing.session.TestSessionState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис прохождения теста студентом.
 * <p>
 * Работает в двух режимах:
 * <ul>
 *     <li>{@link #startAttempt} — создаёт состояние сессии: фиксирует время
 *         начала, один раз перемешивает варианты ответа для каждого вопроса
 *         и возвращает {@link TestSessionState} для хранения в HTTP-сессии;</li>
 *     <li>{@link #finishAttempt} — считает правильные ответы, ставит оценку
 *         и <em>только в этот момент</em> пишет запись в таблицу {@code attempts}.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final QuestionService questionService;
    private final TestService testService;
    private final AnswerShuffleService shuffleService;
    private final GradingService gradingService;

    /**
     * Создаёт состояние попытки в сессии. В БД ничего не записывает.
     * Перемешивает варианты ответа один раз — дальше порядок фиксирован.
     */
    public TestSessionState startAttempt(Long testId) {
        Test test = testService.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found: " + testId));

        List<Question> questions = questionService.findByTestId(test.getId());
        if (questions.isEmpty()) {
            throw new IllegalStateException("В тесте нет вопросов");
        }

        TestSessionState state = new TestSessionState();
        state.setTestId(test.getId());
        state.setStartTime(LocalDateTime.now());

        List<TestSessionState.QuestionEntry> entries = new ArrayList<>();
        for (Question q : questions) {
            TestSessionState.QuestionEntry entry = new TestSessionState.QuestionEntry();
            entry.setQuestionId(q.getId());
            entry.setWording(q.getWording());
            entry.setCorrectAnswer(q.getCorrectAns());
            entry.setShuffledOptions(shuffleService.shuffle(q.allAnswers()));
            entry.setSelectedAnswer(null);
            entries.add(entry);
        }
        state.setQuestions(entries);
        return state;
    }

    /**
     * Сохраняет выбранный ответ на указанный вопрос (1-based index).
     * Ничего не пишет в БД.
     */
    public void recordAnswer(TestSessionState state, int questionNumber, String selectedAnswer) {
        int idx = questionNumber - 1;
        if (idx < 0 || idx >= state.getQuestions().size()) {
            throw new IllegalArgumentException("Invalid question number: " + questionNumber);
        }
        state.getQuestions().get(idx).setSelectedAnswer(selectedAnswer);
    }

    /**
     * Завершает попытку: считает правильные ответы, выставляет оценку,
     * записывает строку в таблицу {@code attempts} и возвращает её.
     */
    @Transactional
    public Attempt finishAttempt(TestSessionState state, String studentName) {
        Test test = testService.findById(state.getTestId())
                .orElseThrow(() -> new IllegalArgumentException("Test not found: " + state.getTestId()));

        int correct = countCorrect(state);
        int total = state.getQuestions().size();
        short grade = gradingService.grade(correct, total);

        Attempt attempt = Attempt.builder()
                .name(studentName)
                .test(test)
                .correctAns(correct)
                .grade(grade)
                .startTime(state.getStartTime())
                .endTime(LocalDateTime.now())
                .build();
        return attemptRepository.save(attempt);
    }

    /**
     * Подсчёт количества правильных ответов в состоянии попытки.
     * Незатронутый вопрос ({@code selectedAnswer == null}) считается неверным.
     */
    public int countCorrect(TestSessionState state) {
        int correct = 0;
        for (TestSessionState.QuestionEntry e : state.getQuestions()) {
            if (e.getSelectedAnswer() != null
                    && e.getSelectedAnswer().equals(e.getCorrectAnswer())) {
                correct++;
            }
        }
        return correct;
    }

    @Transactional(readOnly = true)
    public Optional<Attempt> findById(Long id) {
        return attemptRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Attempt> findAllOrderByEndTimeDesc() {
        return attemptRepository.findAllByOrderByEndTimeDesc();
    }

    /**
     * Возвращает количество вопросов в тесте, к которому привязана попытка.
     * Использует COUNT-запрос, чтобы не триггерить ленивую загрузку коллекции
     * за пределами транзакции.
     */
    @Transactional(readOnly = true)
    public long countQuestionsForAttempt(Attempt attempt) {
        return questionService.countByTestId(attempt.getTest().getId());
    }
}
