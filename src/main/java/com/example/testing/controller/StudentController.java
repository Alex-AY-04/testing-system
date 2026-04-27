package com.example.testing.controller;

import com.example.testing.entity.Attempt;
import com.example.testing.entity.Test;
import com.example.testing.service.AttemptService;
import com.example.testing.service.TestService;
import com.example.testing.session.TestSessionState;
import com.example.testing.validation.StudentNameValidator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Контроллер студенческого потока: ввод имени, список тестов, прохождение,
 * экран результата. Никакой авторизации — только валидация имени в сессии.
 */
@Controller
@RequiredArgsConstructor
public class StudentController {

    private static final String SESSION_NAME = "studentName";
    private static final String SESSION_STATE = "testSessionState";

    private final StudentNameValidator nameValidator;
    private final TestService testService;
    private final AttemptService attemptService;

    /**
     * Принимает имя студента с главной страницы. Валидирует регекс,
     * при ошибке возвращает на главную с флеш-сообщением.
     */
    @PostMapping("/student/name")
    public String submitName(@RequestParam("fullName") String fullName,
                             HttpSession session,
                             RedirectAttributes ra) {
        String trimmed = fullName == null ? "" : fullName.trim();
        if (!nameValidator.isValid(trimmed)) {
            ra.addFlashAttribute("nameError",
                    "Введите имя и фамилию через пробел (до 30 символов каждое, только буквы и дефис).");
            ra.addFlashAttribute("enteredName", trimmed);
            return "redirect:/";
        }
        session.setAttribute(SESSION_NAME, trimmed);
        return "redirect:/student/tests";
    }

    @GetMapping("/student/tests")
    public String listTests(HttpSession session, Model model) {
        String name = (String) session.getAttribute(SESSION_NAME);
        if (name == null) {
            return "redirect:/";
        }
        List<Test> tests = testService.findAll();
        model.addAttribute("tests", tests);
        model.addAttribute("studentName", name);
        return "student/tests";
    }

    /**
     * Стартует попытку: создаёт состояние в сессии, редиректит на первый вопрос.
     */
    @PostMapping("/student/attempts/start")
    public String startAttempt(@RequestParam("testId") Long testId,
                               HttpSession session) {
        String name = (String) session.getAttribute(SESSION_NAME);
        if (name == null) {
            return "redirect:/";
        }
        TestSessionState state = attemptService.startAttempt(testId);
        session.setAttribute(SESSION_STATE, state);
        return "redirect:/student/attempts/current?question=1";
    }

    @GetMapping("/student/attempts/current")
    public String showQuestion(@RequestParam(value = "question", defaultValue = "1") int questionNumber,
                               HttpSession session,
                               Model model) {
        String name = (String) session.getAttribute(SESSION_NAME);
        TestSessionState state = (TestSessionState) session.getAttribute(SESSION_STATE);
        if (name == null || state == null) {
            return "redirect:/student/tests";
        }

        int total = state.getQuestions().size();
        if (questionNumber < 1) questionNumber = 1;
        if (questionNumber > total) questionNumber = total;

        TestSessionState.QuestionEntry current = state.getQuestions().get(questionNumber - 1);

        model.addAttribute("studentName", name);
        model.addAttribute("currentNumber", questionNumber);
        model.addAttribute("totalQuestions", total);
        model.addAttribute("current", current);
        model.addAttribute("questions", state.getQuestions());
        model.addAttribute("isFirst", questionNumber == 1);
        model.addAttribute("isLast", questionNumber == total);
        return "student/question";
    }

    /**
     * AJAX-эндпоинт: сохраняет выбранный ответ в сессии без редиректа.
     * Вызывается из JS при клике по радио-кнопке, чтобы ответ сохранялся
     * сразу, а не при нажатии «Далее».
     */
    @PostMapping("/student/attempts/answer")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Void> saveAnswer(
            @RequestParam("currentNumber") int currentNumber,
            @RequestParam("selectedAnswer") String selectedAnswer,
            HttpSession session) {
        TestSessionState state = (TestSessionState) session.getAttribute(SESSION_STATE);
        if (state == null) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
        attemptService.recordAnswer(state, currentNumber, selectedAnswer);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    /**
     * Сохраняет ответ в сессии и перекидывает на указанный вопрос.
     * Используется для кнопок «Назад», «Далее» и клика по номеру пагинации.
     */
    @PostMapping("/student/attempts/navigate")
    public String navigate(@RequestParam("currentNumber") int currentNumber,
                           @RequestParam(value = "selectedAnswer", required = false) String selectedAnswer,
                           @RequestParam("target") int target,
                           HttpSession session) {
        TestSessionState state = (TestSessionState) session.getAttribute(SESSION_STATE);
        if (state == null) {
            return "redirect:/student/tests";
        }
        if (selectedAnswer != null && !selectedAnswer.isEmpty()) {
            attemptService.recordAnswer(state, currentNumber, selectedAnswer);
        }
        return "redirect:/student/attempts/current?question=" + target;
    }

    /**
     * Завершает тест: пишет попытку в БД, очищает состояние сессии,
     * редиректит на страницу результата.
     */
    @PostMapping("/student/attempts/finish")
    public String finishAttempt(@RequestParam("currentNumber") int currentNumber,
                                @RequestParam(value = "selectedAnswer", required = false) String selectedAnswer,
                                HttpSession session) {
        String name = (String) session.getAttribute(SESSION_NAME);
        TestSessionState state = (TestSessionState) session.getAttribute(SESSION_STATE);
        if (name == null || state == null) {
            return "redirect:/student/tests";
        }
        if (selectedAnswer != null && !selectedAnswer.isEmpty()) {
            attemptService.recordAnswer(state, currentNumber, selectedAnswer);
        }
        Attempt saved = attemptService.finishAttempt(state, name);
        session.removeAttribute(SESSION_STATE);
        return "redirect:/student/attempts/" + saved.getId() + "/result";
    }

    @GetMapping("/student/attempts/{id}/result")
    public String showResult(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                             HttpSession session,
                             Model model) {
        String name = (String) session.getAttribute(SESSION_NAME);
        if (name == null) {
            return "redirect:/";
        }
        Attempt attempt = attemptService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + id));
        // Используем count-запрос вместо ленивой коллекции, чтобы не ловить LazyInitializationException
        int total = (int) attemptService.countQuestionsForAttempt(attempt);
        model.addAttribute("attempt", attempt);
        model.addAttribute("total", total);
        model.addAttribute("studentName", name);
        return "student/result";
    }
}
