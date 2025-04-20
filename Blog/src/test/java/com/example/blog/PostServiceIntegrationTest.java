package com.example.blog;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.services.AccountService;
import com.example.blog.services.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PostServiceIntegrationTest {             // Створив акк, створив публікацію до цього аккаунту, перевірив чи все норм, видалив та переівив шо і як

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @Test
    public void testCreateAndDeletePost() {

        Account account = new Account();
        account.setEmail("test@example.com");
        account.setPassword("password");
        account.setFirstName("Test");
        account.setLastName("User");
        account = accountService.save(account);


        Post post = new Post();
        post.setTitle("Test Post");
        post.setBody("This is a test post.");
        post.setAccount(account);
        post = postService.save(post);

        // отримав публікацію
        assertNotNull(post.getId());


        postService.delete(post);


        assertFalse(postService.getById(post.getId()).isPresent());
    }
}
