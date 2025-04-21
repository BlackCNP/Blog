package com.example.blog.repositories;

import com.example.blog.models.Account; // Додайте, якщо потрібно для findByLikedByContains
import com.example.blog.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- Додайте імпорт
import org.springframework.data.repository.query.Param; // <-- Додайте імпорт
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Додайте, якщо потрібно для findLikeCountById

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Метод для пошуку постів за автором (з попередніх кроків)
    List<Post> findByAccountIdOrderByIdDesc(Long accountId);

    // --- ДОДАНО: Методи для лайків ---

    /**
     * Перевіряє, чи лайкнув вказаний користувач (за ID) вказаний пост (за ID).
     * Використовує COUNT для ефективності, щоб не завантажувати сутності.
     * @param postId ID поста.
     * @param accountId ID користувача.
     * @return true, якщо користувач лайкнув пост, інакше false.
     */
    @Query("SELECT COUNT(p) > 0 FROM Post p JOIN p.likedBy a WHERE p.id = :postId AND a.id = :accountId")
    boolean existsByIdAndLikedByAccountId(@Param("postId") Long postId, @Param("accountId") Long accountId);

    /**
     * Повертає кількість лайків для вказаного поста.
     * Використовує size() в HQL/JPQL для ефективності.
     * @param postId ID поста.
     * @return Кількість лайків (Integer). Повертає 0, якщо пост не знайдено або немає лайків.
     */
    @Query("SELECT size(p.likedBy) FROM Post p WHERE p.id = :postId")
    Integer findLikeCountByPostId(@Param("postId") Long postId);


    /**
     * Знаходить усі пости, які були вподобані вказаним користувачем.
     * @param accountId ID користувача.
     * @return Список постів (List<Post>), які вподобав користувач.
     */
    @Query("SELECT p FROM Post p JOIN p.likedBy a WHERE a.id = :accountId ORDER BY p.createdAt DESC") // Додав сортування за датою
    List<Post> findPostsLikedByAccountId(@Param("accountId") Long accountId);

    // --- КІНЕЦЬ ДОДАНИХ МЕТОДІВ ---
}