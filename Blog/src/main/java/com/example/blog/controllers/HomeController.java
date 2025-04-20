package com.example.blog.controllers;

import com.example.blog.models.Post;
import com.example.blog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Tag(name = "Home Controller", description = "The main entry point for the Kursovav4 application")
public class HomeController {
    @Autowired
    private PostService postService;

    @Operation(summary = "Get all posts", description = "Retrieves a list of all available posts and displays them on the home page.")
    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.getAll();
        model.addAttribute("posts", posts);
        return "home";
    }
}
