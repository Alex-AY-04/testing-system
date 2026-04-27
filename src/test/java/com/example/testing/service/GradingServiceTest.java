package com.example.testing.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты формулы выставления оценки.
 * Проверяются все пограничные случаи: ровно 0.50, 0.65, 0.80, 1.00,
 * а также значения чуть ниже границ (0.49, 0.64, 0.79).
 */
class GradingServiceTest {

    private final GradingService service = new GradingService();

    /**
     * 100% правильных ответов должны давать максимальную оценку 5.
     * Это верхняя граница диапазона «отлично».
     */
    @Test
    void returnsFiveForPerfectScore() {
        assertEquals((short) 5, service.grade(10, 10));
    }

    /**
     * Ровно 80% (8 из 10) — нижняя граница диапазона «отлично»,
     * должно попасть в оценку 5, а не 4.
     */
    @Test
    void returnsFiveAtLowerBoundOfExcellent() {
        assertEquals((short) 5, service.grade(8, 10));
    }

    /**
     * Ровно 79% — чуть ниже границы 80%. Должна выставляться оценка 4.
     * Используем 79 из 100, чтобы получить ровное значение.
     */
    @Test
    void returnsFourJustBelowExcellent() {
        assertEquals((short) 4, service.grade(79, 100));
    }

    /**
     * Ровно 65% (13 из 20) — нижняя граница диапазона «хорошо»,
     * должно попасть в оценку 4, а не 3.
     */
    @Test
    void returnsFourAtLowerBoundOfGood() {
        assertEquals((short) 4, service.grade(13, 20));
    }

    /**
     * Ровно 64% — чуть ниже границы «хорошо». Должна выставляться оценка 3.
     */
    @Test
    void returnsThreeJustBelowGood() {
        assertEquals((short) 3, service.grade(64, 100));
    }

    /**
     * Ровно 50% (5 из 10) — нижняя граница «удовлетворительно»,
     * должно попасть в оценку 3, а не 2.
     */
    @Test
    void returnsThreeAtLowerBoundOfSatisfactory() {
        assertEquals((short) 3, service.grade(5, 10));
    }

    /**
     * Ровно 49% — чуть ниже границы «удовлетворительно», должно дать 2.
     */
    @Test
    void returnsTwoJustBelowSatisfactory() {
        assertEquals((short) 2, service.grade(49, 100));
    }

    /**
     * 0 правильных из N должно давать минимальную оценку 2.
     */
    @Test
    void returnsTwoForZeroCorrect() {
        assertEquals((short) 2, service.grade(0, 10));
    }

    /**
     * Негативные кейсы: нулевое или отрицательное число вопросов — ошибка.
     */
    @Test
    void throwsWhenTotalIsZero() {
        assertThrows(IllegalArgumentException.class, () -> service.grade(0, 0));
    }

    /**
     * Негативные кейсы: число правильных ответов не может превышать общее.
     */
    @Test
    void throwsWhenCorrectExceedsTotal() {
        assertThrows(IllegalArgumentException.class, () -> service.grade(11, 10));
    }

    /**
     * Негативные кейсы: отрицательное число правильных ответов — ошибка.
     */
    @Test
    void throwsWhenCorrectIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> service.grade(-1, 10));
    }
}
