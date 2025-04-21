package com.example.blog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula; // <-- Додано імпорт

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_like", // Перевірте, чи ця назва правильна!
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private Set<Account> likedBy = new HashSet<>();

    // --- ДОДАНО: Поле для сортування за лайками ---
    /**
     * Це поле не зберігається в таблиці Post, а обчислюється
     * базою даних при кожному запиті завдяки @Formula.
     * Дозволяє сортувати пости за кількістю лайків.
     * Важливо: "post_like" - це назва вашої проміжної таблиці для лайків.
     * "pl.post_id" - назва колонки в цій таблиці, що посилається на Post.
     * "id" - назва колонки ID в таблиці Post.
     */
    @Formula("(select count(*) from post_like pl where pl.post_id = id)")
    private int likeCount; // Hibernate автоматично заповнить це поле
    // --- КІНЕЦЬ ДОДАНОГО КОДУ ---


    @Override
    public String toString() {
        // Оновлений toString може включати likeCount, якщо потрібно,
        // але основна логіка залишається
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", accountId=" + (account != null ? account.getId() : null) +
                ", actualLikes=" + (likedBy != null ? likedBy.size() : 0) + // Реальний розмір колекції (може бути LAZY)
                ", calculatedLikeCount=" + likeCount + // Значення з @Formula
                '}';
    }

    // Методи addLiker/removeLiker залишаються
    public void addLiker(Account account) {
        this.likedBy.add(account);
    }

    public void removeLiker(Account account) {
        this.likedBy.remove(account);
    }

}