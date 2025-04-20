package com.example.blog.repositories;



import com.example.blog.models.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void whenFindOneByEmail_thenReturnAccount() {

        Account test = new Account();
        test.setEmail("test@test.test");
        entityManager.persist(test);
        entityManager.flush();


        Optional<Account> found = accountRepository.findOneByEmail(test.getEmail());


        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getEmail()).isEqualTo(test.getEmail());
    }
}
