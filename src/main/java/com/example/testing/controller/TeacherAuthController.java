package com.example.testing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TeacherAuthController {

    @GetMapping("/teacher/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Неверный логин или пароль");
        }
        if (logout != null) {
            model.addAttribute("loggedOut", true);
        }
        return "teacher/login";
    }
}
