package com.example.blog.services;

import com.example.blog.models.Account;
import com.example.blog.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Додав імпорт для readOnly

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Метод save (залишається без змін)
    public Account save(Account account) {
        if (account.getPassword() != null && !account.getPassword().startsWith("$2a$")) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        return accountRepository.save(account);
    }

    // Метод findByEmail (залишається без змін)
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

    // Метод findById (залишається без змін)
    @Transactional(readOnly = true)
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    // --- ДОДАНО: Метод existsById ---
    /**
     * Перевіряє, чи існує акаунт із вказаним ID.
     * Делегує виклик до AccountRepository.
     * @param id ID акаунта для перевірки.
     * @return true, якщо акаунт існує, інакше false.
     */
    @Transactional(readOnly = true) // Цей метод теж тільки читає дані
    public boolean existsById(Long id) {
        return accountRepository.existsById(id);
    }
    // --- КІНЕЦЬ ДОДАНОГО МЕТОДУ ---

}