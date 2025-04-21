package com.example.blog.controllers;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.services.AccountService;
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
// --- ДОДАНО ІМПОРТ ---
import org.springframework.security.access.prepost.PreAuthorize;
// --- ---
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    // Метод для сторінки автора
    @GetMapping("/author/{authorId}")
    @Operation(summary = "Отримати всі пости автора", description = "Показати сторінку з усіма постами певного автора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сторінку успішно відображено"),
            @ApiResponse(responseCode = "404", description = "Автора не знайдено")
    })
    public String getPostsByAuthor(@PathVariable Long authorId, Model model) {
        Optional<Account> optionalAuthor = accountService.findById(authorId);
        if (optionalAuthor.isEmpty()) {
            model.addAttribute("errorMessage", "Author with ID " + authorId + " not found.");
            return "pomilka";
        }
        Account author = optionalAuthor.get();
        List<Post> posts = postService.findPostsByAuthorId(authorId);
        model.addAttribute("author", author);
        model.addAttribute("posts", posts);
        return "author_posts";
    }

    // Метод для отримання одного поста
    @GetMapping("/posts/{id}")
    @Operation(summary = "Отримати пост за ID", description = "Отримати пост з бази даних за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пост успішно отримано"),
            @ApiResponse(responseCode = "404", description = "Пост не знайдено")
    })
    public String getPost(@PathVariable Long id, Model model, Principal principal) {
        Optional<Post> optionalPost = this.postService.getById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);

            boolean isOwner = false;
            boolean isAdmin = false;

            if (principal != null) {
                String username = principal.getName();
                if (post.getAccount() != null && post.getAccount().getEmail().equalsIgnoreCase(username)) {
                    isOwner = true;
                }
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                // Перевіряємо роль тут, використовуючи повне ім'я ролі
                if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    isAdmin = true;
                }
                accountService.findByEmail(username)
                        .ifPresent(account -> model.addAttribute("currentUserAccount", account));
            }
            // Додаємо прапорець в модель
            model.addAttribute("canEditDelete", isAdmin || isOwner);

            return "post";
        } else {
            return "pomilka";
        }
    }

    // Метод для сторінки створення поста
    @GetMapping("/posts/create")
    @PreAuthorize("isAuthenticated()") // Потрібен імпорт
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

    // Метод для обробки створення поста
    @PostMapping("/posts/create")
    @PreAuthorize("isAuthenticated()") // Потрібен імпорт
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


    // Метод для сторінки редагування
    @GetMapping("/posts/{id}/edit")
    @PreAuthorize("isAuthenticated()") // Потрібен імпорт
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
            } else {
                return "pomilka";
            }
        } else {
            return "pomilka";
        }
    }

    // Метод для обробки оновлення поста
    @PostMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()") // Потрібен імпорт
    @Operation(summary = "Оновити пост за ID", description = "Оновити деталі поста з бази даних за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Редирект на сторінку поста"),
            @ApiResponse(responseCode = "400", description = "Помилка даних форми"),
            @ApiResponse(responseCode = "403", description = "Заборонено"),
            @ApiResponse(responseCode = "404", description = "Пост не знайдено")
    })
    public String updatePost(@PathVariable Long id, @ModelAttribute Post post, BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            post.setId(id);
            model.addAttribute("post", post);
            return "post_edit";
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
            } else {
                return "pomilka";
            }
        } else {
            return "pomilka";
        }
    }

    // Метод для видалення поста
    @GetMapping("/posts/{id}/delete")
    @PreAuthorize("isAuthenticated()") // Потрібен імпорт
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
            } else {
                return "pomilka";
            }
        } else {
            return "pomilka";
        }
    }
}