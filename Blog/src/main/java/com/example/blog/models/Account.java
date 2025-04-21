package com.example.blog.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List; // Залишаємо List для posts, якщо це потрібно
import java.util.Set;   // Додано імпорт

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String email;
    private String password;
    private String firstName;
    private String lastName;

    // Зв'язок з постами, написаними користувачем
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Додав cascade та LAZY
    private List<Post> posts; // Можливо, тут теж краще Set? Але залежить від потреб.

    // Зв'язок з ролями
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})
    private Set<Authority> authorities = new HashSet<>();


    // --- ДОДАНО: Поле для зберігання вподобаних постів ---
    @ManyToMany(mappedBy = "likedBy", fetch = FetchType.LAZY) // mappedBy вказує на поле в Post
    private Set<Post> likedPosts = new HashSet<>(); // Множина постів, які лайкнув користувач
    // --- КІНЕЦЬ ДОДАНОГО КОДУ ---


    @Override
    public String toString() {
        // Оновлений toString, щоб не включати колекції за замовчуванням,
        // бо це може призвести до рекурсії або завантаження LAZY полів.
        return "Account{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                // ", authorities=" + authorities + // Закоментовано, щоб уникнути проблем з LAZY/рекурсією
                // ", posts_count=" + (posts != null ? posts.size() : 0) + // Приклад виводу кількості
                // ", likedPosts_count=" + (likedPosts != null ? likedPosts.size() : 0) + // Приклад виводу кількості
                '}';
    }

    // Потрібно перевизначити hashCode() та equals(), якщо ви працюєте з @ManyToMany.
    // Lombok @EqualsAndHashCode(exclude = {"posts", "likedPosts", "authorities"}) може допомогти.
    // Або згенерувати їх через IDE, зазвичай на основі поля 'id'.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id); // Порівняння за ID, якщо він не null
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this); // Хеш-код на основі ID або пам'яті
    }
}