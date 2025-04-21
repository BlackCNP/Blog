package com.example.blog.controllers;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.services.AccountService; // Додано
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal; // Додано
import java.util.HashMap;      // Додано
import java.util.List;
import java.util.Map;          // Додано
import java.util.Optional;      // Додано

@Controller
@Tag(name = "Home Controller", description = "The main entry point for the Kursovav4 application")
public class HomeController {

    @Autowired
    private PostService postService;

    @Autowired // Додано
    private AccountService accountService;

    @Operation(summary = "Get all posts", description = "Retrieves a list of all available posts and displays them on the home page.")
    @GetMapping("/")
    public String home(Model model, Principal principal) { // Додано Principal
        List<Post> posts = postService.getAll(); // Припускаємо, що це повертає всі пости

        // --- Додаємо інформацію про лайки і статус для поточного користувача ---
        Map<Long, Integer> likeCounts = new HashMap<>();
        Map<Long, Boolean> userLikedStatuses = new HashMap<>();
        Account currentUser = null;
        Long currentUserId = null;

        // Отримуємо дані поточного користувача (якщо він авторизований)
        if (principal != null) {
            Optional<Account> optCurrentUser = accountService.findByEmail(principal.getName());
            if (optCurrentUser.isPresent()) {
                currentUser = optCurrentUser.get();
                currentUserId = currentUser.getId();
                model.addAttribute("currentUserAccount", currentUser); // Для хедера
            }
        }

        // Проходимо по всіх постах для отримання даних про лайки
        // УВАГА: Це може спричинити N+1 проблему запитів!
        for (Post post : posts) {
            likeCounts.put(post.getId(), postService.getLikeCount(post.getId()));
            boolean liked = false;
            if (currentUserId != null) {
                // Перевіряємо, чи поточний користувач лайкнув пост
                liked = postService.hasUserLikedPost(post.getId(), currentUserId);
            }
            userLikedStatuses.put(post.getId(), liked);
        }
        // --- Кінець додавання інформації ---

        // Додаємо все до моделі
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);             // Map<postId, count>
        model.addAttribute("userLikedStatuses", userLikedStatuses); // Map<postId, boolean>

        return "home";
    }
}