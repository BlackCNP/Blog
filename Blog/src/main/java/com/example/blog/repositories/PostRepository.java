package com.example.blog.repositories;

import com.example.blog.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Додайте цей імпорт

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // --- ДОДАЙТЕ ЦЕЙ МЕТОД ---
    /**
     * Знаходить всі пости, що належать певному автору за його ID,
     * сортуючи їх за ID в спадному порядку (від нових до старих).
     * @param accountId ID автора (Account).
     * @return Список постів автора.
     */
    List<Post> findByAccountIdOrderByIdDesc(Long accountId);
    // --- КІНЕЦЬ ДОДАНОГО МЕТОДУ ---
}