package com.example.blog.controllers;

import com.example.blog.models.Account;
import com.example.blog.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        if (!model.containsAttribute("account")) {
            model.addAttribute("account", new Account());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerNewUser(@Valid @ModelAttribute("account") Account account,
                                  BindingResult bindingResult,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            accountService.save(account);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.startsWith(AccountService.MSG_PREFIX_EMAIL_EXISTS)) {

                    String userMessage = message.substring(AccountService.MSG_PREFIX_EMAIL_EXISTS.length());
                    bindingResult.rejectValue("email", "error.account.email", userMessage);
                } else if (message.startsWith(AccountService.MSG_PREFIX_NICKNAME_EXISTS)) {
                    String userMessage = message.substring(AccountService.MSG_PREFIX_NICKNAME_EXISTS.length());
                    bindingResult.rejectValue("firstName", "error.account.firstName", userMessage);
                } else {

                    model.addAttribute("registrationError", "Невідома помилка валідації.");
                }
            } else {
                model.addAttribute("registrationError", "Сталася помилка під час реєстрації.");
            }
            return "register";
        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage().toLowerCase();
            boolean errorProcessed = false;

            if (rootCauseMessage.contains("account_email_key") || rootCauseMessage.contains("constraint violation for public.account(email)")) {
                bindingResult.rejectValue("email", "error.db.email", "Ця пошта вже зареєстрована (DB).");
                errorProcessed = true;
            }
            if (rootCauseMessage.contains("account_firstname_key") || rootCauseMessage.contains("constraint violation for public.account(firstname)")) {
                bindingResult.rejectValue("firstName", "error.db.firstName", "Цей псевдонім вже використовується (DB).");
                errorProcessed = true;
            }

            if (!errorProcessed) {
                model.addAttribute("registrationError", "Помилка реєстрації: дані вже існують (DB).");
            }
            return "register";
        } catch (Exception e) {
            model.addAttribute("registrationError", "Сталася неочікувана помилка.");
            return "register";
        }
    }
}