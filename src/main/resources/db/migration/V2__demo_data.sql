-- V2__demo_data.sql
-- Демо-данные для быстрой проверки приложения.

INSERT INTO tests (id, name) VALUES (1, 'Основы Java');
INSERT INTO tests (id, name) VALUES (2, 'Математика: арифметика');

-- === Вопросы для теста «Основы Java» ===
INSERT INTO questions (test_id, wording, correct_ans, incorrect_ans1, incorrect_ans2, incorrect_ans3) VALUES
(1, 'Какое ключевое слово используется для наследования класса в Java?',
    'extends', 'inherits', 'implements', 'super'),
(1, 'Какой примитивный тип имеет размер 64 бита?',
    'long', 'int', 'short', 'byte'),
(1, 'Что вернёт выражение 10 / 3 в Java, если оба операнда целочисленные?',
    '3', '3.33', '3.3333', '4'),
(1, 'Какой модификатор доступа делает поле видимым только внутри того же класса?',
    'private', 'public', 'protected', 'default'),
(1, 'Какой интерфейс является корневым для коллекций типа List и Set?',
    'Collection', 'Iterable', 'Map', 'Iterator');

-- === Вопросы для теста «Математика: арифметика» ===
INSERT INTO questions (test_id, wording, correct_ans, incorrect_ans1, incorrect_ans2, incorrect_ans3) VALUES
(2, 'Сколько будет 7 × 8?',
    '56', '54', '58', '64'),
(2, 'Чему равен квадрат числа 12?',
    '144', '124', '142', '169'),
(2, 'Сколько будет 125 ÷ 5?',
    '25', '20', '30', '15');

-- === Демо-попытки (для живого вида страницы преподавателя) ===
INSERT INTO attempts (name, test_id, correct_ans, grade, start_time, end_time) VALUES
('Иван Петров',      1, 5, 5, TIMESTAMP '2025-04-15 10:00:00', TIMESTAMP '2025-04-15 10:08:12'),
('Мария Сидорова',   1, 3, 3, TIMESTAMP '2025-04-15 11:00:00', TIMESTAMP '2025-04-15 11:09:45'),
('John Smith',       2, 2, 4, TIMESTAMP '2025-04-16 09:30:00', TIMESTAMP '2025-04-16 09:33:20');

-- Сбрасываем sequences, т.к. мы вставили id 1 и 2 вручную
SELECT setval('tests_id_seq',    (SELECT MAX(id) FROM tests));
SELECT setval('questions_id_seq', (SELECT MAX(id) FROM questions));
SELECT setval('attempts_id_seq',  (SELECT MAX(id) FROM attempts));
