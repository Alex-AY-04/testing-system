package com.example.testing.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты валидатора имени и фамилии студента.
 * Проверяются все типы допустимых и недопустимых входов.
 */
class StudentNameValidatorTest {

    private final StudentNameValidator validator = new StudentNameValidator();

    /**
     * Валидное кириллическое имя и фамилия — штатный сценарий.
     */
    @Test
    void acceptsCyrillicFullName() {
        assertTrue(validator.isValid("Иван Петров"));
    }

    /**
     * Валидное латинское имя и фамилия — поддержка иностранных студентов.
     */
    @Test
    void acceptsLatinFullName() {
        assertTrue(validator.isValid("John Smith"));
    }

    /**
     * Дефис в фамилии допустим (например, составные фамилии).
     */
    @Test
    void acceptsHyphenInSurname() {
        assertTrue(validator.isValid("Мария Петрова-Иванова"));
    }

    /**
     * Цифры в имени запрещены и должны отклоняться.
     */
    @Test
    void rejectsDigits() {
        assertFalse(validator.isValid("Ivan1 Petrov"));
    }

    /**
     * Спецсимволы запрещены.
     */
    @Test
    void rejectsSpecialCharacters() {
        assertFalse(validator.isValid("Ivan! Petrov"));
    }

    /**
     * Без пробела — это одно слово, не соответствует формату «Имя Фамилия».
     */
    @Test
    void rejectsSingleWord() {
        assertFalse(validator.isValid("Иван"));
    }

    /**
     * Два пробела между словами — невалидно (требуется ровно один).
     */
    @Test
    void rejectsTwoSpaces() {
        assertFalse(validator.isValid("Иван  Петров"));
    }

    /**
     * Превышение длины 30 символов — отклоняется.
     */
    @Test
    void rejectsTooLongName() {
        String longName = "a".repeat(31);
        assertFalse(validator.isValid(longName + " Petrov"));
    }

    /**
     * Ровно 30 символов в имени и 30 в фамилии — на границе, должно быть валидно.
     */
    @Test
    void acceptsExactlyMaxLength() {
        String name = "a".repeat(30);
        String surname = "b".repeat(30);
        assertTrue(validator.isValid(name + " " + surname));
    }

    /**
     * null на входе не должен ломать валидатор — просто false.
     */
    @Test
    void rejectsNull() {
        assertFalse(validator.isValid(null));
    }

    /**
     * Пустая строка — невалидно.
     */
    @Test
    void rejectsEmpty() {
        assertFalse(validator.isValid(""));
    }

    /**
     * Три слова (два пробела) — невалидно.
     */
    @Test
    void rejectsThreeWords() {
        assertFalse(validator.isValid("Иван Петр Иванов"));
    }

    /**
     * Пробел в начале или конце — невалидно.
     */
    @Test
    void rejectsLeadingOrTrailingSpace() {
        assertFalse(validator.isValid(" Иван Петров"));
        assertFalse(validator.isValid("Иван Петров "));
    }
}
