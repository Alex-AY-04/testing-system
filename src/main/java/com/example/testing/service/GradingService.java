package com.example.testing.service;

import org.springframework.stereotype.Service;

/**
 * Сервис выставления оценки за тест по формуле:
 * <pre>
 *   ratio = correctAnswers / totalQuestions
 *   ratio ∈ [0.80, 1.00] → 5
 *   ratio ∈ [0.65, 0.79] → 4
 *   ratio ∈ [0.50, 0.64] → 3
 *   ratio < 0.50         → 2
 * </pre>
 * Границы сравниваются с небольшим эпсилоном, чтобы избежать артефактов
 * арифметики с плавающей точкой (например, 0.65 ровно).
 */
@Service
public class GradingService {

    private static final double EPS = 1e-9;

    /**
     * Считает оценку по числу правильных ответов и общему числу вопросов.
     *
     * @param correct количество правильных ответов (>= 0)
     * @param total   общее число вопросов (> 0)
     * @return оценка от 2 до 5
     * @throws IllegalArgumentException если {@code total <= 0} или {@code correct < 0}
     *                                  или {@code correct > total}
     */
    public short grade(int correct, int total) {
        if (total <= 0) {
            throw new IllegalArgumentException("total must be positive");
        }
        if (correct < 0 || correct > total) {
            throw new IllegalArgumentException("correct must be in [0, total]");
        }
        double ratio = (double) correct / total;
        if (ratio >= 0.80 - EPS) {
            return 5;
        }
        if (ratio >= 0.65 - EPS) {
            return 4;
        }
        if (ratio >= 0.50 - EPS) {
            return 3;
        }
        return 2;
    }
}
