package com.example.blog.config;

import com.example.blog.models.Account;
import com.example.blog.models.Authority;
import com.example.blog.models.Post;
import com.example.blog.repositories.AuthorityRepository;
import com.example.blog.services.AccountService;
import com.example.blog.services.PostService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public void run(String... args) throws Exception {


        List<Post> posts = postService.getAllSortedAndFiltered("date", null);


        if (posts.isEmpty()) {

            Authority user = new Authority();
            user.setName("ROLE_USER");
            authorityRepository.save(user);

            Authority admin = new Authority();
            admin.setName("ROLE_ADMIN");
            authorityRepository.save(admin);

            Account account1 = new Account();
            account1.setFirstName("user");
            account1.setLastName("user");
            account1.setEmail("user@hello.world");
            account1.setPassword("password");
            Set<Authority> authorities1 = new HashSet<>();
            authorityRepository.findById("ROLE_USER").ifPresent(authorities1::add);
            account1.setAuthorities(authorities1);

            Account account2 = new Account();
            account2.setFirstName("admin");
            account2.setLastName("admin");
            account2.setEmail("admi.imba@hello.world"); // valeriy@va.com
            account2.setPassword("password");
            Set<Authority> authorities2 = new HashSet<>();
            authorityRepository.findById("ROLE_ADMIN").ifPresent(authorities2::add);
            authorityRepository.findById("ROLE_USER").ifPresent(authorities2::add);
            account2.setAuthorities(authorities2);

            Account adminAcc1 = new Account();
            adminAcc1.setFirstName("Admin_jr");
            adminAcc1.setLastName("Admin");
            adminAcc1.setEmail("admi.imba1@hello.world");
            adminAcc1.setPassword("password");
            authorityRepository.findById("ROLE_ADMIN");

            Account adminAcc2 = new Account();
            adminAcc2.setFirstName("Moderator_jr");
            adminAcc2.setLastName("Admin");
            adminAcc2.setEmail("admi.imba2@hello.world");
            adminAcc2.setPassword("password");
            authorityRepository.findById("ROLE_ADMIN");

            Account adminAcc3 = new Account();
            adminAcc3.setFirstName("Moderator");
            adminAcc3.setLastName("Admin");
            adminAcc3.setEmail("admi.imba3@hello.world");
            adminAcc3.setPassword("password");
            authorityRepository.findById("ROLE_ADMIN");

            Account adminAcc4 = new Account();
            adminAcc4.setFirstName("Administrator");
            adminAcc4.setLastName("Admin");
            adminAcc4.setEmail("admi.imba4@hello.world");
            adminAcc4.setPassword("password");
            authorityRepository.findById("ROLE_ADMIN");


            accountService.save(account1);
            accountService.save(account2);
            accountService.save(adminAcc1);
            accountService.save(adminAcc2);
            accountService.save(adminAcc3);
            accountService.save(adminAcc4);

            Account savedAccount1 = accountService.findByEmail("user@hello.world").orElse(null);
            Account savedAccount2 = accountService.findByEmail("admi.imba@hello.world").orElse(null);

            Post post1 = new Post();
            post1.setTitle("Пост 1");
            post1.setBody("Тута контент");
            post1.setAccount(savedAccount1);

            Post post2 = new Post();
            post2.setTitle("Вітаємо");
            post2.setBody("Вітаємо на нашій блог-платформі");
            post2.setAccount(savedAccount2);

            postService.save(post1);
            postService.save(post2);


            if (savedAccount1 != null && savedAccount2 != null && post1.getId() != null && post2.getId() != null) {
                try {
                    postService.likePost(post1.getId(), savedAccount2.getId()); // admin лайкає пост 1
                    postService.likePost(post2.getId(), savedAccount1.getId()); // user  пост 2
                    postService.likePost(post2.getId(), savedAccount2.getId()); // admin пост 2
                } catch (EntityNotFoundException e) {
                    System.err.println("Error adding seed likes: " + e.getMessage());
                }
            } else {
                System.err.println("Could not add seed likes because accounts or posts were not saved correctly.");
            }
        }
    }
}