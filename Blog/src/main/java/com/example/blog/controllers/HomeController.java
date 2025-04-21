package com.example.blog.controllers;

import com.example.blog.models.Account; // Додайте імпорт
import com.example.blog.models.Post;
import com.example.blog.services.AccountService; // Додайте імпорт
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal; // Додайте імпорт
import java.util.List;
import java.util.Optional; // Додайте імпорт

@Controller
@Tag(name = "Home Controller", description = "The main entry point for the Kursovav4 application")
public class HomeController {

    @Autowired
    private PostService postService;

    @Autowired // Переконайтесь, що AccountService підключено
    private AccountService accountService;

    @Operation(summary = "Get all posts", description = "Retrieves a list of all available posts and displays them on the home page.")
    @GetMapping("/")
    // Додаємо Principal principal до аргументів методу
    public String home(Model model, Principal principal) {
        List<Post> posts = postService.getAll();
        model.addAttribute("posts", posts);

        // --- ДОДАНО: Отримання даних поточного користувача ---
        if (principal != null) {
            String email = principal.getName(); // Отримуємо email поточного користувача
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            if (optionalAccount.isPresent()) {
                // Якщо акаунт знайдено, додаємо його до моделі
                model.addAttribute("currentUserAccount", optionalAccount.get());
            }
            // Якщо акаунт не знайдено (малоймовірно для авторизованого користувача),
            // атрибут "currentUserAccount" просто не буде додано до моделі.
        }
        // --- КІНЕЦЬ ДОДАНОГО КОДУ ---

        return "home";
    }
}