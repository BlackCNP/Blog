package com.example.blog.repositories;

import com.example.blog.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findOneByEmail(String email);
    Optional<Account> findByFirstName(String firstName);
}
