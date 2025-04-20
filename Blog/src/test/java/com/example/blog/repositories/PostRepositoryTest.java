package com.example.blog.repositories;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void whenFindById_thenReturnPost() {

        Account account = new Account();
        account.setEmail("test@test.com");
        entityManager.persist(account);
        entityManager.flush();

        Post post = new Post();
        post.setTitle("Test Post");
        post.setBody("This is a test post.");
        post.setAccount(account);
        entityManager.persist(post);
        entityManager.flush();


        Optional<Post> found = postRepository.findById(post.getId());


        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getTitle()).isEqualTo(post.getTitle());
    }
}
