package com.example.testing.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор имени и фамилии студента.
 * <p>
 * Требования:
 * <ul>
 *     <li>ровно два слова, разделённых одним пробелом;</li>
 *     <li>каждое слово — от 1 до 30 символов;</li>
 *     <li>допустимы буквы кириллицы, латиницы и дефис;</li>
 *     <li>цифры и прочие спецсимволы запрещены.</li>
 * </ul>
 */
@Component
public class StudentNameValidator {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-zА-Яа-яЁё\\-]{1,30} [A-Za-zА-Яа-яЁё\\-]{1,30}$");

    /**
     * Возвращает {@code true}, если переданная строка соответствует
     * формату «Имя Фамилия». Метод тривиально устойчив к {@code null}.
     */
    public boolean isValid(String fullName) {
        if (fullName == null) {
            return false;
        }
        return PATTERN.matcher(fullName).matches();
    }
}
