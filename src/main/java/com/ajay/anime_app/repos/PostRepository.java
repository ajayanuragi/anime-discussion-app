package com.ajay.anime_app.repos;

import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, Long> {

    Post findFirstByUser(User user);

    List<Post> findByUserId(Long userId);

    Post findByIdAndIsDeletedFalse(Long id);

    List<Post> findByIsDeletedFalse(Sort id);
}
