package com.example.blog.config;


import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable) // Відключення CSRF (для H2 консолі та API, якщо немає токенів)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // Дозволити H2 консоль в iframe
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                antMatcher("/css/**"),
                                antMatcher("/js/**"),
                                antMatcher("/fonts/**"),
                                antMatcher("/webjars/**"),
                                antMatcher("/"), // Головна сторінка
                                antMatcher("/rss/**"),
                                antMatcher("/register/**"), // Сторінка реєстрації
                                antMatcher("/login"), // Сторінка логіну
                                antMatcher("/h2-console/**"), // H2 консоль
                                antMatcher("/swagger-ui/**"), // Swagger UI V3 (перевірте точний шлях)
                                antMatcher("/v3/api-docs/**"), // Swagger JSON/YAML
                                antMatcher("/posts/**"), // Всі сторінки постів (перегляд, створення GET)
                                antMatcher("/pomilka"), // Сторінка помилки

                                antMatcher("/author/**"),

                                PathRequest.toH2Console() // Альтернативний спосіб дозволити H2
                        ).permitAll()

                        .anyRequest().authenticated() // Всі інші запити вимагають автентифікації
                )
                .formLogin(form -> form
                        .loginPage("/login") // Кастомна сторінка логіну
                        .loginProcessingUrl("/login") // URL для обробки логіну
                        .usernameParameter("email") // Параметр для email
                        .passwordParameter("password") // Параметр для паролю
                        .defaultSuccessUrl("/", true) // Завжди редірект на головну після успішного входу
                        .failureUrl("/login?error") // URL при помилці логіну
                        .permitAll() // Дозволити всім доступ до сторінки логіну та обробки
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .permitAll() // Дозволити всім вихід
                );

        return http.build();
    }
}