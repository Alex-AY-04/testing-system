package com.example.testing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация Spring Security.
 * <p>
 * Защищает все маршруты /teacher/** кроме страницы логина.
 * Логин и пароль преподавателя берутся из {@link TeacherCredentialsProperties}
 * и хэшируются BCrypt при создании бина {@link UserDetailsService}.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TeacherCredentialsProperties teacherProps;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails teacher = User.builder()
                .username(teacherProps.getUsername())
                .password(encoder.encode(teacherProps.getPassword()))
                .roles("TEACHER")
                .build();
        return new InMemoryUserDetailsManager(teacher);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/teacher/login").permitAll()
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/teacher/login")
                        .loginProcessingUrl("/teacher/login")
                        .defaultSuccessUrl("/teacher/dashboard", true)
                        .failureUrl("/teacher/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/teacher/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }
}
