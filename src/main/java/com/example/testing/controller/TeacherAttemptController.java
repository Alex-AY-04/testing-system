package com.example.testing.controller;

import com.example.testing.entity.Attempt;
import com.example.testing.service.AttemptService;
import com.example.testing.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Страница просмотра попыток всех студентов для преподавателя.
 * Сортировка — от новых к старым.
 */
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherAttemptController {

    private final AttemptService attemptService;
    private final QuestionService questionService;

    @GetMapping("/attempts")
    @Transactional(readOnly = true)
    public String listAttempts(Model model) {
        List<Attempt> attempts = attemptService.findAllOrderByEndTimeDesc();

        // Кешируем кол-во вопросов по test_id, чтобы один тест не считался N раз.
        Map<Long, Long> totalsByTestId = new HashMap<>();
        List<AttemptRow> rows = new ArrayList<>();
        for (Attempt a : attempts) {
            Long testId = a.getTest().getId();
            String testName = a.getTest().getName(); // read inside transaction (lazy)
            long total = totalsByTestId.computeIfAbsent(testId, questionService::countByTestId);
            rows.add(new AttemptRow(a.getId(), testName, a.getName(),
                    a.getCorrectAns(), (int) total, a.getGrade(), a.getEndTime()));
        }
        model.addAttribute("rows", rows);
        return "teacher/attempts";
    }

    /** Плоская строка таблицы, чтобы шаблон не дергал ленивые поля. */
    public record AttemptRow(
            Long id,
            String testName,
            String studentName,
            Integer correctAns,
            int total,
            Short grade,
            java.time.LocalDateTime endTime
    ) {}
}
