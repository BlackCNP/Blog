package com.example.blog.services;

import com.example.blog.models.Account;
import com.example.blog.models.Post;
import com.example.blog.repositories.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AccountService accountService;


    public Optional<Post> getById(Long id) { return postRepository.findById(id); }
    public Post save(Post post) { if (post.getId() == null) { post.setCreatedAt(LocalDateTime.now()); } post.setModifiedAt(LocalDateTime.now()); return postRepository.save(post); }
    public void delete(Post post) { postRepository.delete(post); }
    public List<Post> findPostsByAuthorId(Long authorId) { return postRepository.findByAccountIdOrderByIdDesc(authorId); }
    @Transactional
    public void likePost(Long postId, Long userId) { Post post = postRepository.findById(postId) .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId)); Account liker = accountService.findById(userId) .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + userId)); post.getLikedBy().add(liker); }
    @Transactional
    public void unlikePost(Long postId, Long userId) { Post post = postRepository.findById(postId) .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId)); Account unliker = accountService.findById(userId) .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + userId)); post.getLikedBy().remove(unliker); }
    @Transactional(readOnly = true) public boolean hasUserLikedPost(Long postId, Long userId) { if (!postRepository.existsById(postId) || !accountService.existsById(userId)) { return false; } return postRepository.existsByIdAndLikedByAccountId(postId, userId); }
    @Transactional(readOnly = true) public int getLikeCount(Long postId) { Integer count = postRepository.findLikeCountByPostId(postId); return count != null ? count : 0; }
    @Transactional(readOnly = true) public List<Post> getLikedPostsByUser(Long userId) { return postRepository.findPostsLikedByAccountId(userId); }



    @Transactional(readOnly = true)
    public List<Post> getAllSortedAndFiltered(String sortBy, String searchQuery) {
        Sort sort;

        if ("likes".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "likeCount", "createdAt");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }



        if (StringUtils.hasText(searchQuery)) {

            return postRepository.findByTitleContainingIgnoreCase(searchQuery.trim(), sort);
        } else {

            return postRepository.findAll(sort);
        }
    }


}