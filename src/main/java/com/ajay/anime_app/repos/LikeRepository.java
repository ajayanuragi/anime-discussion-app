package com.ajay.anime_app.repos;

import com.ajay.anime_app.domain.Like;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Id> {
    Optional<Like> findByPostId(Long postId);

    void deleteByUserIdAndPostId(long userId, Long postId);
}
