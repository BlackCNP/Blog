package com.example.blog.controllers;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.services.AccountService;
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;


    @PostMapping("/api/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> likePostApi(@PathVariable Long postId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {

            Account currentUser = accountService.findByEmail(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
            Long userId = currentUser.getId();


            postService.likePost(postId, userId);


            int newLikeCount = postService.getLikeCount(postId);

            response.put("success", true);
            response.put("likeCount", newLikeCount);
            response.put("userLiked", true);
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


    @PostMapping("/api/posts/{postId}/unlike")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
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
            response.put("userLiked", false);
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


    @GetMapping("/posts/liked")
    @PreAuthorize("isAuthenticated()")
    public String showLikedPosts(Model model, Principal principal) {
        try {
            Account currentUser = accountService.findByEmail(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
            Long userId = currentUser.getId();

            List<Post> likedPosts = postService.getLikedPostsByUser(userId);


            Map<Long, Integer> likeCounts = new HashMap<>();
            Map<Long, Boolean> userLikedStatuses = new HashMap<>();

            for (Post post : likedPosts) {
                likeCounts.put(post.getId(), postService.getLikeCount(post.getId()));

                userLikedStatuses.put(post.getId(), true);
            }


            model.addAttribute("posts", likedPosts);
            model.addAttribute("likeCounts", likeCounts);
            model.addAttribute("userLikedStatuses", userLikedStatuses);
            model.addAttribute("currentUserAccount", currentUser);
            model.addAttribute("pageTitle", "Вподобані публікації");

            return "liked_posts";

        } catch (EntityNotFoundException e) {

            return "pomilka";
        }
    }



    @GetMapping("/author/{authorId}")


    public String getPostsByAuthor(@PathVariable Long authorId, Model model, Principal principal) {
        Optional<Account> optionalAuthor = accountService.findById(authorId);
        if (optionalAuthor.isEmpty()) { /* ... обробка помилки ... */ return "pomilka"; }

        Account author = optionalAuthor.get();
        List<Post> posts = postService.findPostsByAuthorId(authorId);


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
            likeCounts.put(post.getId(), postService.getLikeCount(post.getId()));
            boolean liked = false;
            if (currentUserId != null) {
                liked = postService.hasUserLikedPost(post.getId(), currentUserId);
            }
            userLikedStatuses.put(post.getId(), liked);
        }


        model.addAttribute("author", author);
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLikedStatuses", userLikedStatuses);

        return "author_posts";
    }



    @GetMapping("/posts/{id}")

    public String getPost(@PathVariable Long id, Model model, Principal principal) {
        Optional<Post> optionalPost = this.postService.getById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);

            boolean isOwner = false;
            boolean isAdmin = false;
            boolean userLiked = false;
            Account currentUser = null;


            if (principal != null) {
                String username = principal.getName();
                Optional<Account> optCurrentUser = accountService.findByEmail(username);
                if (optCurrentUser.isPresent()) {
                    currentUser = optCurrentUser.get();
                    model.addAttribute("currentUserAccount", currentUser);


                    if (post.getAccount() != null && post.getAccount().getId().equals(currentUser.getId())) {
                        isOwner = true;
                    }

                    userLiked = postService.hasUserLikedPost(post.getId(), currentUser.getId());
                }


                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    isAdmin = true;
                }
            }


            model.addAttribute("canEditDelete", isAdmin || isOwner);
            model.addAttribute("likeCount", postService.getLikeCount(post.getId()));
            model.addAttribute("userLiked", userLiked);

            return "post";
        } else {
            return "pomilka";
        }
    }


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