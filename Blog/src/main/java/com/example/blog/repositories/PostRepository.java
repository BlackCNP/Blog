package com.example.blog.repositories;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import org.springframework.data.domain.Sort; // <-- Додайте імпорт
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Метод для пошуку постів за автором
    List<Post> findByAccountIdOrderByIdDesc(Long accountId);

    // Методи для лайків
    @Query("SELECT COUNT(p) > 0 FROM Post p JOIN p.likedBy a WHERE p.id = :postId AND a.id = :accountId")
    boolean existsByIdAndLikedByAccountId(@Param("postId") Long postId, @Param("accountId") Long accountId);

    @Query("SELECT size(p.likedBy) FROM Post p WHERE p.id = :postId")
    Integer findLikeCountByPostId(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p JOIN p.likedBy a WHERE a.id = :accountId ORDER BY p.createdAt DESC")
    List<Post> findPostsLikedByAccountId(@Param("accountId") Long accountId);

    // пошук за назв
    List<Post> findByTitleContainingIgnoreCase(String title, Sort sort);


}