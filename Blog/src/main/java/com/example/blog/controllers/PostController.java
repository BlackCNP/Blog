package com.example.blog.controllers;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.services.AccountService;
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException; // Додано для обробки помилок
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Додано для відповіді API
import org.springframework.http.ResponseEntity; // Додано для відповіді API
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*; // Додано для @ResponseBody

import java.security.Principal;
import java.util.HashMap; // Додано для відповіді API
import java.util.List;
import java.util.Map; // Додано для відповіді API
import java.util.Optional;
import java.util.stream.Collectors; // Додано для обробки списків


@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    // --- ДОДАНО: API Ендпоінт для ЛАЙКА ---
    @PostMapping("/api/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody // Важливо: вказує, що метод повертає дані (JSON), а не ім'я шаблону
    public ResponseEntity<?> likePostApi(@PathVariable Long postId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Отримуємо ID поточного користувача
            Account currentUser = accountService.findByEmail(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
            Long userId = currentUser.getId();

            // Викликаємо сервіс для лайка
            postService.likePost(postId, userId);

            // Отримуємо оновлену кількість лайків
            int newLikeCount = postService.getLikeCount(postId);

            response.put("success", true);
            response.put("likeCount", newLikeCount);
            response.put("userLiked", true); // Користувач тепер лайкнув
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            // Обробка інших можливих помилок
            response.put("success", false);
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- ДОДАНО: API Ендпоінт для АНЛАЙКА ---
    @PostMapping("/api/posts/{postId}/unlike")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody // Повертаємо JSON
    public ResponseEntity<?> unlikePostApi(@PathVariable Long postId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account currentUser = accountService.findByEmail(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
            Long userId = currentUser.getId();

            postService.unlikePost(postId, userId);

            int newLikeCount = postService.getLikeCount(postId);

            response.put("success", true);
            response.put("likeCount", newLikeCount);
            response.put("userLiked", false); // Користувач тепер не лайкнув
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- ДОДАНО: Ендпоінт для сторінки "Вподобані пости" ---
    @GetMapping("/posts/liked")
    @PreAuthorize("isAuthenticated()")
    public String showLikedPosts(Model model, Principal principal) {
        try {
            Account currentUser = accountService.findByEmail(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
            Long userId = currentUser.getId();

            List<Post> likedPosts = postService.getLikedPostsByUser(userId);

            // --- Додаємо інформацію про лайки і статус для поточного користувача ---
            //    (Це може здатися дивним для сторінки "вподобане", але потрібно для
            //     коректної роботи іконки лайка, яка буде на всіх сторінках)
            Map<Long, Integer> likeCounts = new HashMap<>();
            Map<Long, Boolean> userLikedStatuses = new HashMap<>(); // Тут завжди буде true

            for (Post post : likedPosts) {
                likeCounts.put(post.getId(), postService.getLikeCount(post.getId()));
                // На цій сторінці всі пости лайкнуті поточним користувачем
                userLikedStatuses.put(post.getId(), true);
            }
            // --- Кінець додавання інформації ---

            model.addAttribute("posts", likedPosts); // Використовуємо ім'я "posts" для уніфікації з іншими шаблонами
            model.addAttribute("likeCounts", likeCounts);
            model.addAttribute("userLikedStatuses", userLikedStatuses);
            model.addAttribute("currentUserAccount", currentUser); // Для хедера
            model.addAttribute("pageTitle", "Вподобані публікації"); // Заголовок сторінки

            return "liked_posts"; // Назва нового шаблону

        } catch (EntityNotFoundException e) {
            // Обробка помилки, якщо користувач не знайдений
            // model.addAttribute("errorMessage", e.getMessage());
            return "pomilka"; // Або інша сторінка помилки
        }
    }


    // --- Оновлений Метод getPostsByAuthor ---
    @GetMapping("/author/{authorId}")
    @Operation(summary = "Отримати всі пости автора", description = "Показати сторінку з усіма постами певного автора")
    @ApiResponses(value = { /* ... */ })
    // Додаємо Principal
    public String getPostsByAuthor(@PathVariable Long authorId, Model model, Principal principal) {
        Optional<Account> optionalAuthor = accountService.findById(authorId);
        if (optionalAuthor.isEmpty()) { /* ... обробка помилки ... */ return "pomilka"; }

        Account author = optionalAuthor.get();
        List<Post> posts = postService.findPostsByAuthorId(authorId);

        // --- Додаємо інформацію про лайки і статус для поточного користувача ---
        Map<Long, Integer> likeCounts = new HashMap<>();
        Map<Long, Boolean> userLikedStatuses = new HashMap<>();
        Account currentUser = null;
        Long currentUserId = null;

        if (principal != null) {
            Optional<Account> optCurrentUser = accountService.findByEmail(principal.getName());
            if (optCurrentUser.isPresent()) {
                currentUser = optCurrentUser.get();
                currentUserId = currentUser.getId();
                model.addAttribute("currentUserAccount", currentUser); // Для хедера
            }
        }

        for (Post post : posts) {
            likeCounts.put(post.getId(), postService.getLikeCount(post.getId()));
            boolean liked = false;
            if (currentUserId != null) {
                liked = postService.hasUserLikedPost(post.getId(), currentUserId);
            }
            userLikedStatuses.put(post.getId(), liked);
        }
        // --- Кінець додавання інформації ---

        model.addAttribute("author", author);
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLikedStatuses", userLikedStatuses);

        return "author_posts";
    }


    // --- Оновлений Метод getPost ---
    @GetMapping("/posts/{id}")
    @Operation(summary = "Отримати пост за ID", description = "Отримати пост з бази даних за його ID")
    @ApiResponses(value = { /* ... */ })
    public String getPost(@PathVariable Long id, Model model, Principal principal) {
        Optional<Post> optionalPost = this.postService.getById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);

            boolean isOwner = false;
            boolean isAdmin = false;
            boolean userLiked = false;
            Account currentUser = null;

            // Перевіряємо авторизацію та отримуємо дані
            if (principal != null) {
                String username = principal.getName();
                Optional<Account> optCurrentUser = accountService.findByEmail(username);
                if (optCurrentUser.isPresent()) {
                    currentUser = optCurrentUser.get();
                    model.addAttribute("currentUserAccount", currentUser); // Додаємо для хедера

                    // Перевіряємо власника
                    if (post.getAccount() != null && post.getAccount().getId().equals(currentUser.getId())) {
                        isOwner = true;
                    }
                    // Перевіряємо, чи лайкнув поточний користувач
                    userLiked = postService.hasUserLikedPost(post.getId(), currentUser.getId());
                }

                // Перевіряємо адміна
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    isAdmin = true;
                }
            }

            // Додаємо прапорці та кількість лайків в модель
            model.addAttribute("canEditDelete", isAdmin || isOwner);
            model.addAttribute("likeCount", postService.getLikeCount(post.getId()));
            model.addAttribute("userLiked", userLiked);

            return "post";
        } else {
            return "pomilka";
        }
    }

    // --- Решта методів (create, edit, update, delete) залишаються як у попередній версії ---
    // ... (скопіюйте їх сюди з попереднього повідомлення) ...
    @GetMapping("/posts/create")
    @PreAuthorize("isAuthenticated()")
    public String createNewPost(Model model, Principal principal) {
        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }
        Optional<Account> optionalAccount = accountService.findByEmail(authUsername);
        if (optionalAccount.isPresent()) {
            Post post = new Post();
            post.setAccount(optionalAccount.get());
            model.addAttribute("post", post);
            return "post_create";
        } else {
            return "pomilka";
        }
    }

    @PostMapping("/posts/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Створити новий пост", description = "Створити новий пост у базі даних")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Редирект на сторінку поста"),
            @ApiResponse(responseCode = "400", description = "Помилка даних форми"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    public String createNewPost(@ModelAttribute Post post, BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            return "post_create";
        }
        String authUsername = principal.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(authUsername);
        if (optionalAccount.isPresent()) {
            if(post.getAccount() == null || !post.getAccount().getId().equals(optionalAccount.get().getId())) {
                post.setAccount(optionalAccount.get());
            }
        } else {
            return "pomilka";
        }
        postService.save(post);
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Отримати пост для редагування", description = "Отримати пост з бази даних для редагування за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішне отримання поста"),
            @ApiResponse(responseCode = "403", description = "Заборонено"),
            @ApiResponse(responseCode = "404", description = "Пост не знайдено")
    })
    public String getPostForEdit(@PathVariable Long id, Model model, Principal principal) {
        String authUsername = principal.getName();
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (post.getAccount() != null && (post.getAccount().getEmail().equalsIgnoreCase(authUsername) || isAdmin)) {
                model.addAttribute("post", post);
                return "post_edit";
            } else { return "pomilka"; }
        } else { return "pomilka"; }
    }

    @PostMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Оновити пост за ID", description = "Оновити деталі поста з бази даних за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Редирект на сторінку поста"),
            @ApiResponse(responseCode = "400", description = "Помилка даних форми"),
            @ApiResponse(responseCode = "403", description = "Заборонено"),
            @ApiResponse(responseCode = "404", description = "Пост не знайдено")
    })
    public String updatePost(@PathVariable Long id, @ModelAttribute Post post, BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            post.setId(id); model.addAttribute("post", post); return "post_edit";
        }
        String authUsername = principal.getName();
        Optional<Post> optionalExistingPost = postService.getById(id);
        if (optionalExistingPost.isPresent()) {
            Post existingPost = optionalExistingPost.get();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (existingPost.getAccount() != null && (existingPost.getAccount().getEmail().equalsIgnoreCase(authUsername) || isAdmin)) {
                existingPost.setTitle(post.getTitle());
                existingPost.setBody(post.getBody());
                postService.save(existingPost);
                return "redirect:/posts/" + existingPost.getId();
            } else { return "pomilka"; }
        } else { return "pomilka"; }
    }

    @GetMapping("/posts/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Видалити пост за ID", description = "Видалити існуючий пост з бази даних за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Редирект на головну"),
            @ApiResponse(responseCode = "403", description = "Заборонено"),
            @ApiResponse(responseCode = "404", description = "Пост не знайдено")
    })
    public String deletePost(@PathVariable Long id, Principal principal) {
        String authUsername = principal.getName();
        Optional<Post> optionalPost = postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (post.getAccount() != null && (post.getAccount().getEmail().equalsIgnoreCase(authUsername) || isAdmin)) {
                postService.delete(post);
                return "redirect:/";
            } else { return "pomilka"; }
        } else { return "pomilka"; }
    }

}