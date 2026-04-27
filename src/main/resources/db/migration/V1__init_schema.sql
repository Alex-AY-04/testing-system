-- V1__init_schema.sql
-- Начальная схема системы тестирования.

CREATE TABLE tests (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE questions (
    id              BIGSERIAL PRIMARY KEY,
    test_id         BIGINT NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    wording         TEXT   NOT NULL,
    correct_ans     TEXT   NOT NULL,
    incorrect_ans1  TEXT   NOT NULL,
    incorrect_ans2  TEXT   NOT NULL,
    incorrect_ans3  TEXT   NOT NULL
);

CREATE INDEX idx_questions_test_id ON questions(test_id);

CREATE TABLE attempts (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(61) NOT NULL,
    test_id     BIGINT NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    correct_ans INTEGER NOT NULL,
    grade       SMALLINT NOT NULL,
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL
);

CREATE INDEX idx_attempts_test_id ON attempts(test_id);
CREATE INDEX idx_attempts_end_time ON attempts(end_time DESC);
