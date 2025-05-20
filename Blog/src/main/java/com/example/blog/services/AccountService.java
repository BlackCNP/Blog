package com.example.blog.services;

import com.example.blog.models.Account;
import com.example.blog.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static final String MSG_PREFIX_EMAIL_EXISTS = "ERROR_EMAIL_EXISTS:";
    public static final String MSG_PREFIX_NICKNAME_EXISTS = "ERROR_NICKNAME_EXISTS:";

    @Transactional
    public Account save(Account account) {
        if (account.getId() == null) {
            Optional<Account> existingByEmail = accountRepository.findOneByEmail(account.getEmail());
            if (existingByEmail.isPresent()) {
                throw new IllegalArgumentException(MSG_PREFIX_EMAIL_EXISTS + "Пошта '" + account.getEmail() + "' вже використовується.");
            }

            Optional<Account> existingByFirstName = accountRepository.findByFirstName(account.getFirstName());
            if (existingByFirstName.isPresent()) {
                throw new IllegalArgumentException(MSG_PREFIX_NICKNAME_EXISTS + "Псевдонім '" + account.getFirstName() + "' вже використовується.");
            }

            if (account.getPassword() != null && !account.getPassword().isEmpty()) {
                account.setPassword(passwordEncoder.encode(account.getPassword()));
            }
        } else {
            Account existingAccountData = accountRepository.findById(account.getId())
                    .orElseThrow(() -> new RuntimeException("Акаунт для оновлення не знайдено: " + account.getId()));

            if (account.getPassword() != null && !account.getPassword().isEmpty()) {
                if (!account.getPassword().startsWith("$2a$")) {
                    account.setPassword(passwordEncoder.encode(account.getPassword()));
                }
            } else {
                account.setPassword(existingAccountData.getPassword());
            }
        }
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return accountRepository.existsById(id);
    }
}