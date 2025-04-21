package com.example.blog.controllers;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.services.AccountService;
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Tag(name = "Home Controller", description = "The main entry point for the Kursovav4 application")
public class HomeController {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @Operation(summary = "Get all posts", description = "Retrieves a list of all available posts and displays them on the home page.")
    @GetMapping("/")
    public String home(Model model, Principal principal,
                       // Додаємо параметр сортування
                       @RequestParam(name = "sort", defaultValue = "date", required = false) String sortBy,
                       // --- ДОДАНО: Параметр для пошуку ---
                       @RequestParam(name = "query", required = false) String query) {

        // Викликаємо оновлений метод сервісу з сортуванням ТА пошуком
        List<Post> posts = postService.getAllSortedAndFiltered(sortBy, query);

        // --- Логіка отримання лайків (без змін) ---
        Map<Long, Integer> likeCounts = new HashMap<>();
        Map<Long, Boolean> userLikedStatuses = new HashMap<>();
        Account currentUser = null;
        Long currentUserId = null;
        if (principal != null) {
            Optional<Account> optCurrentUser = accountService.findByEmail(principal.getName());
            if (optCurrentUser.isPresent()) {
                currentUser = optCurrentUser.get();
                currentUserId = currentUser.getId();
                model.addAttribute("currentUserAccount", currentUser);
            }
        }
        for (Post post : posts) {
            likeCounts.put(post.getId(), post.getLikeCount());
            boolean liked = false;
            if (currentUserId != null) {
                liked = postService.hasUserLikedPost(post.getId(), currentUserId);
            }
            userLikedStatuses.put(post.getId(), liked);
        }
        // --- Кінець логіки лайків ---

        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLikedStatuses", userLikedStatuses);
        model.addAttribute("currentSort", sortBy); // Передаємо поточне сортування
        // --- ДОДАНО: Передача пошукового запиту в модель ---
        model.addAttribute("searchQuery", query); // Передаємо поточний запит

        return "home";
    }
}