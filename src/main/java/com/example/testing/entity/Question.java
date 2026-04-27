package com.example.testing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "test")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false, columnDefinition = "text")
    private String wording;

    @Column(name = "correct_ans", nullable = false, columnDefinition = "text")
    private String correctAns;

    @Column(name = "incorrect_ans1", nullable = false, columnDefinition = "text")
    private String incorrectAns1;

    @Column(name = "incorrect_ans2", nullable = false, columnDefinition = "text")
    private String incorrectAns2;

    @Column(name = "incorrect_ans3", nullable = false, columnDefinition = "text")
    private String incorrectAns3;

    /**
     * Возвращает список из всех четырёх вариантов ответа в исходном порядке:
     * [правильный, неправильный 1, неправильный 2, неправильный 3].
     */
    public List<String> allAnswers() {
        return List.of(correctAns, incorrectAns1, incorrectAns2, incorrectAns3);
    }
}
