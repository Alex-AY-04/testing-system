package com.example.testing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для формы ввода имени и фамилии студента на главной странице.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentNameDto {

    @NotBlank(message = "Имя не может быть пустым")
    private String fullName;
}
