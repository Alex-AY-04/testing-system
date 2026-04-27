package com.example.testing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO формы создания/редактирования теста.
 * Содержит название теста и список вопросов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestFormDto {

    private Long id;
    private String name;
    private List<QuestionFormDto> questions = new ArrayList<>();
}
