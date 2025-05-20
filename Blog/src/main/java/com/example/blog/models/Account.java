package com.example.blog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @NotBlank(message = "Пошта не може бути порожньою")
    @Email(message = "Некоректний формат пошти")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    @Size(min = 8, message = "Пароль має містити щонайменше 8 символів")
    private String password;

    @NotBlank(message = "Псевдонім не може бути порожнім")
    @Size(min = 4, max = 20, message = "Псевдонім має містити від 4 до 20 символів")
    @Column(unique = true, nullable = false) // Унікальність для ніка (firstName)
    private String firstName;

    @NotBlank(message = "Ім'я не може бути порожнім")
    @Size(max = 20, message = "Ім'я має містити не більше 20 символів")
    private String lastName;

    // Звязок з постами написаними користувачем
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})
    private Set<Authority> authorities = new HashSet<>();



    @ManyToMany(mappedBy = "likedBy", fetch = FetchType.LAZY)
    private Set<Post> likedPosts = new HashSet<>();



    @Override
    public String toString() {

        return "Account{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +

                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}