package com.example.blog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet; // <-- Додано імпорт
import java.util.Set;   // <-- Додано імпорт

@Entity
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id; // Зробив приватним для кращої інкапсуляції

    private String title; // Зробив приватним

    @Column(columnDefinition = "TEXT")
    private String body; // Зробив приватним

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY) // Додав LAZY fetch для автора
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    // --- ДОДАНО: Поле для зберігання лайків ---
    @ManyToMany(fetch = FetchType.LAZY) // LAZY - щоб не завантажувати всіх юзерів одразу
    @JoinTable(
            name = "post_like", // Назва проміжної таблиці
            joinColumns = @JoinColumn(name = "post_id"), // Зовнішній ключ до таблиці Post
            inverseJoinColumns = @JoinColumn(name = "account_id") // Зовнішній ключ до таблиці Account
    )
    private Set<Account> likedBy = new HashSet<>(); // Множина користувачів, які лайкнули пост
    // --- КІНЕЦЬ ДОДАНОГО КОДУ ---


    // toString() краще генерувати через Lombok або IDE, щоб включити нові поля,
    // але поки залишаємо ваш варіант, щоб не заважати.
    @Override
    public String toString() {
        return "Post{" + // Змінив назву класу в toString
                "id=" + id +
                ", title='" + title + '\'' +
                // Не рекомендується виводити весь body в toString
                // ", body='" + body + '\'' +
                ", createdAt=" + createdAt + // Прибрав одинарні лапки для дат
                ", modifiedAt=" + modifiedAt + // Прибрав одинарні лапки
                ", accountId=" + (account != null ? account.getId() : null) + // Виводимо ID автора
                ", likes=" + (likedBy != null ? likedBy.size() : 0) + // Додав кількість лайків
                '}';
    }

    // Додатково: Методи для додавання/видалення лайка (необов'язково, але зручно)
    // Потребують перевірки на null і т.д. в сервісному шарі

    public void addLiker(Account account) {
        this.likedBy.add(account);
    }

    public void removeLiker(Account account) {
        this.likedBy.remove(account);
    }

}