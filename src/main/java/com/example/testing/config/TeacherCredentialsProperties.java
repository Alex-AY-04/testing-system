package com.example.testing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Свойства учётных данных преподавателя, загружаемые из application.yml.
 * Пароль хранится в plain-тексте и хэшируется BCrypt при старте приложения.
 */
@Data
@ConfigurationProperties(prefix = "app.teacher")
public class TeacherCredentialsProperties {

    private String username;
    private String password;
}
