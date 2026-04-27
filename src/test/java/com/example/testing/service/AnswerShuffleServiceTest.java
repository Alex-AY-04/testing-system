package com.example.testing.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты сервиса перемешивания вариантов ответа.
 */
class AnswerShuffleServiceTest {

    private final AnswerShuffleService service = new AnswerShuffleService();

    /**
     * Перемешивание не должно мутировать исходный список — метод возвращает
     * новый экземпляр.
     */
    @Test
    void doesNotMutateInputList() {
        List<String> input = List.of("a", "b", "c", "d");
        List<String> result = service.shuffle(input);
        assertNotSame(input, result);
        assertEquals(List.of("a", "b", "c", "d"), input);
    }

    /**
     * Все исходные варианты должны присутствовать в результате ровно один раз
     * — никаких потерь или дубликатов.
     */
    @Test
    void preservesAllElementsExactlyOnce() {
        List<String> input = List.of("правильный", "неверный1", "неверный2", "неверный3");
        List<String> result = service.shuffle(input);
        assertEquals(4, result.size());
        for (String s : input) {
            assertTrue(result.contains(s), "Missing element after shuffle: " + s);
        }
    }

    /**
     * Детерминированность: при одном и том же seed результат одинаков —
     * это гарантирует, что в сессии порядок не меняется между
     * обращениями (мы используем уже готовый перемешанный список).
     */
    @Test
    void producesDeterministicResultWithSameSeed() {
        List<String> input = List.of("a", "b", "c", "d");
        List<String> r1 = service.shuffle(input, new Random(42L));
        List<String> r2 = service.shuffle(input, new Random(42L));
        assertEquals(r1, r2);
    }

    /**
     * Порядок действительно может отличаться от исходного — хотя бы
     * один из нескольких запусков с разными seed должен дать
     * перестановку, не совпадающую с исходной.
     */
    @Test
    void canProduceOrderDifferentFromInput() {
        List<String> input = List.of("a", "b", "c", "d");
        boolean foundDifferent = false;
        for (long seed = 1; seed <= 50 && !foundDifferent; seed++) {
            List<String> r = service.shuffle(input, new Random(seed));
            if (!r.equals(input)) foundDifferent = true;
        }
        assertTrue(foundDifferent, "За 50 запусков не удалось получить порядок, отличный от исходного");
    }
}
