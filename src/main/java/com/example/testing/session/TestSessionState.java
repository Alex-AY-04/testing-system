package com.example.testing.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Состояние проходимого теста, которое хранится в HTTP-сессии студента.
 * <p>
 * Попытка записывается в БД только в момент нажатия «Завершить тест».
 * До этого всё — выбранные ответы, время начала, перемешанный порядок
 * вариантов — живёт исключительно в сессии.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSessionState implements Serializable {

    private Long testId;
    private LocalDateTime startTime;
    private List<QuestionEntry> questions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionEntry implements Serializable {
        private Long questionId;
        private String wording;
        private String correctAnswer;
        /** Перемешанные варианты ответа. Порядок фиксируется при старте теста. */
        private List<String> shuffledOptions;
        /** Выбранный студентом вариант (текст). {@code null}, пока студент не ответил. */
        private String selectedAnswer;
    }
}
