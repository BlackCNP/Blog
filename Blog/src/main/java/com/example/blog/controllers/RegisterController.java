package com.example.blog.controllers;


import com.example.blog.models.Account;
import com.example.blog.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {


    @Autowired
    private AccountService accountService;




    @GetMapping("/register")

    public String getRegisterPage(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "register";
    }



    @PostMapping("/register")
    @Operation(summary = "Зареєструвати нового користувача", description = "Зареєструвати нового користувача в системі")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішна реєстрація"),
            @ApiResponse(responseCode = "400", description = "Помилка валідації даних"),
            @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера")
    })
    public String registerNewUser(@ModelAttribute Account account) {
        accountService.save(account);
        return "redirect:/";
    }
}
