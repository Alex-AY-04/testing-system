package com.example.testing.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Сервис перемешивания вариантов ответа.
 * <p>
 * Перемешивание происходит один раз — при старте теста. Результат сохраняется
 * в сессии студента и больше не пересчитывается, поэтому при возврате на
 * предыдущий вопрос порядок вариантов не меняется.
 */
@Service
public class AnswerShuffleService {

    private final Random defaultRandom = new Random();

    /**
     * Возвращает новый список с перемешанными вариантами ответа.
     * Исходный список не изменяется.
     */
    public List<String> shuffle(List<String> options) {
        return shuffle(options, defaultRandom);
    }

    /**
     * Вариант с явно переданным {@link Random} — используется в тестах
     * для получения детерминированного результата.
     */
    public List<String> shuffle(List<String> options, Random rng) {
        List<String> copy = new ArrayList<>(options);
        Collections.shuffle(copy, rng);
        return copy;
    }
}
