package com.example.blog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

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
            name = "post_like",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private Set<Account> likedBy = new HashSet<>();


    @Formula("(select count(*) from post_like pl where pl.post_id = id)")
    private int likeCount;



    @Override
    public String toString() {


        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", accountId=" + (account != null ? account.getId() : null) +
                ", actualLikes=" + (likedBy != null ? likedBy.size() : 0) +
                ", calculatedLikeCount=" + likeCount +
                '}';
    }


    public void addLiker(Account account) {
        this.likedBy.add(account);
    }

    public void removeLiker(Account account) {
        this.likedBy.remove(account);
    }

}