package com.example.testing.controller;

import com.example.testing.dto.QuestionFormDto;
import com.example.testing.dto.TestFormDto;
import com.example.testing.entity.Question;
import com.example.testing.entity.Test;
import com.example.testing.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * CRUD-контроллер тестов для преподавателя.
 * Все маршруты защищены Spring Security.
 */
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherTestController {

    private final TestService testService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tests", testService.findAll());
        return "teacher/dashboard";
    }

    @GetMapping("/tests/new")
    public String newTestForm(Model model) {
        TestFormDto form = new TestFormDto();
        // стартовый набор — один пустой вопрос
        form.setQuestions(new ArrayList<>(List.of(new QuestionFormDto())));
        model.addAttribute("form", form);
        model.addAttribute("mode", "new");
        return "teacher/test-form";
    }

    @PostMapping("/tests")
    public String createTest(@ModelAttribute("form") TestFormDto form,
                             RedirectAttributes ra) {
        try {
            testService.save(form);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("formError", e.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/teacher/tests/new";
        }
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/tests/{id}/edit")
    @Transactional(readOnly = true)
    public String editTestForm(@PathVariable("id") Long id, Model model) {
        Test test = testService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found: " + id));
        TestFormDto form = toForm(test);
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        return "teacher/test-form";
    }

    @PostMapping("/tests/{id}")
    public String updateTest(@PathVariable("id") Long id,
                             @ModelAttribute("form") TestFormDto form,
                             RedirectAttributes ra) {
        form.setId(id);
        try {
            testService.save(form);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("formError", e.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/teacher/tests/" + id + "/edit";
        }
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/tests/{id}/delete")
    public String deleteTest(@PathVariable("id") Long id) {
        testService.delete(id);
        return "redirect:/teacher/dashboard";
    }

    private TestFormDto toForm(Test test) {
        TestFormDto form = new TestFormDto();
        form.setId(test.getId());
        form.setName(test.getName());
        List<QuestionFormDto> qs = new ArrayList<>();
        for (Question q : test.getQuestions()) {
            QuestionFormDto dto = new QuestionFormDto();
            dto.setId(q.getId());
            dto.setWording(q.getWording());
            dto.setCorrectAns(q.getCorrectAns());
            dto.setIncorrectAns1(q.getIncorrectAns1());
            dto.setIncorrectAns2(q.getIncorrectAns2());
            dto.setIncorrectAns3(q.getIncorrectAns3());
            qs.add(dto);
        }
        if (qs.isEmpty()) {
            qs.add(new QuestionFormDto());
        }
        form.setQuestions(qs);
        return form;
    }
}
