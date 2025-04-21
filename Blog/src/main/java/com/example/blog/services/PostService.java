package com.example.blog.services;

import com.example.blog.models.Account; // <-- Додайте імпорт
import com.example.blog.models.Post;
import com.example.blog.repositories.PostRepository;
// Додайте виняток, якщо хочете кидати помилки "не знайдено"
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- Додайте імпорт

import java.time.LocalDateTime;
import java.util.Collections; // <-- Додайте імпорт
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired // Додаємо залежність від AccountService
    private AccountService accountService;

    // --- Існуючі методи ---
    public Optional<Post> getById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getAll() {
        // TODO: Розглянути пагінацію та сортування за замовчуванням тут
        return postRepository.findAll();
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setModifiedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public List<Post> findPostsByAuthorId(Long authorId) {
        // Перевірка чи автор існує (можна додати)
        // if (!accountService.findById(authorId).isPresent()) { return Collections.emptyList(); }
        return postRepository.findByAccountIdOrderByIdDesc(authorId);
    }

    // --- ДОДАНО: Методи для роботи з лайками ---

    /**
     * Додає лайк від користувача до поста.
     * @param postId ID поста.
     * @param userId ID користувача, який лайкає.
     * @throws EntityNotFoundException якщо пост або користувач не знайдений.
     */
    @Transactional // Важливо для зміни стану сутності
    public void likePost(Long postId, Long userId) {
        // Знаходимо пост і користувача
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Account liker = accountService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + userId));

        // Додаємо користувача до множини тих, хто лайкнув (Set автоматично уникне дублікатів)
        post.getLikedBy().add(liker);

        // Збереження не є строго обов'язковим завдяки @Transactional,
        // але може бути корисним для ясності або якщо немає каскадних налаштувань.
        // postRepository.save(post);
    }

    /**
     * Видаляє лайк користувача з поста.
     * @param postId ID поста.
     * @param userId ID користувача, чий лайк видаляється.
     * @throws EntityNotFoundException якщо пост або користувач не знайдений.
     */
    @Transactional // Важливо для зміни стану сутності
    public void unlikePost(Long postId, Long userId) {
        // Знаходимо пост і користувача
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Account unliker = accountService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + userId));

        // Видаляємо користувача з множини тих, хто лайкнув
        post.getLikedBy().remove(unliker);

        // postRepository.save(post); // Те саме, що і в likePost
    }

    /**
     * Перевіряє, чи лайкнув даний користувач даний пост.
     * Використовує ефективний запит до репозиторію.
     * @param postId ID поста.
     * @param userId ID користувача.
     * @return true, якщо користувач лайкнув пост, інакше false.
     */
    @Transactional(readOnly = true) // Тільки читання, але транзакція може допомогти з сесією
    public boolean hasUserLikedPost(Long postId, Long userId) {
        // Перевіряємо існування поста і користувача (опціонально, залежить від логіки)
        if (!postRepository.existsById(postId) || !accountService.existsById(userId)) { // Потрібно додати existsById в AccountService/Repository
            return false;
        }
        return postRepository.existsByIdAndLikedByAccountId(postId, userId);
    }


    /**
     * Отримує кількість лайків для поста.
     * Використовує ефективний запит до репозиторію.
     * @param postId ID поста.
     * @return Кількість лайків.
     */
    @Transactional(readOnly = true) // Тільки читання
    public int getLikeCount(Long postId) {
        Integer count = postRepository.findLikeCountByPostId(postId);
        return count != null ? count : 0; // Повертаємо 0, якщо запит повернув null (напр. пост не знайдено)
    }

    /**
     * Отримує список постів, які вподобав користувач.
     * @param userId ID користувача.
     * @return Список вподобаних постів (List<Post>).
     */
    @Transactional(readOnly = true) // Транзакція потрібна для потенційного LAZY fetching всередині Post
    public List<Post> getLikedPostsByUser(Long userId) {
        // Перевірка чи користувач існує (опціонально)
        // if (!accountService.existsById(userId)) { return Collections.emptyList(); }
        return postRepository.findPostsLikedByAccountId(userId);
    }

    // Допоміжний метод для AccountService (якщо його там ще немає)
    // У файлі AccountService.java:
     /*
     @Autowired
     private AccountRepository accountRepository;

     public boolean existsById(Long id) {
         return accountRepository.existsById(id);
     }

     public Optional<Account> findById(Long id) { // Цей вже має бути
        return accountRepository.findById(id);
     }
     */

}