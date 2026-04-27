-- V1__init_schema.sql (H2-compatible)
CREATE TABLE tests (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE questions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_id         BIGINT NOT NULL,
    wording         CLOB   NOT NULL,
    correct_ans     CLOB   NOT NULL,
    incorrect_ans1  CLOB   NOT NULL,
    incorrect_ans2  CLOB   NOT NULL,
    incorrect_ans3  CLOB   NOT NULL,
    CONSTRAINT fk_questions_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

CREATE INDEX idx_questions_test_id ON questions(test_id);

CREATE TABLE attempts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(61) NOT NULL,
    test_id     BIGINT NOT NULL,
    correct_ans INTEGER NOT NULL,
    grade       SMALLINT NOT NULL,
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    CONSTRAINT fk_attempts_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

CREATE INDEX idx_attempts_test_id ON attempts(test_id);
CREATE INDEX idx_attempts_end_time ON attempts(end_time DESC);
