package com.example.testing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO одного вопроса на форме создания/редактирования теста.
 * Все поля — опциональные на уровне биндинга; серверная валидация отбрасывает
 * частично заполненные вопросы (см. {@link #isFullyFilled()}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFormDto {

    private Long id;
    private String wording;
    private String correctAns;
    private String incorrectAns1;
    private String incorrectAns2;
    private String incorrectAns3;

    /**
     * Проверяет, заполнены ли все обязательные поля вопроса (формулировка,
     * правильный и все три неправильных ответа). Пустые/пробельные значения
     * считаются незаполненными.
     */
    public boolean isFullyFilled() {
        return notBlank(wording)
                && notBlank(correctAns)
                && notBlank(incorrectAns1)
                && notBlank(incorrectAns2)
                && notBlank(incorrectAns3);
    }

    /**
     * Проверяет, заполнено ли хоть одно поле вопроса. Используется для
     * отделения полностью пустых блоков от частично заполненных.
     */
    public boolean isEmpty() {
        return !notBlank(wording)
                && !notBlank(correctAns)
                && !notBlank(incorrectAns1)
                && !notBlank(incorrectAns2)
                && !notBlank(incorrectAns3);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
