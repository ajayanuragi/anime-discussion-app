package com.ajay.anime_app.repos;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment findFirstByUser(User user);

    Comment findFirstByPost(Post post);


    List<Comment> findCommentsByPostId(Long postId);

    List<Comment> findByParentComment(Comment parentComment);

    Comment findByIdAndIsDeletedFalse(Long id);


    List<Comment> findByIsDeletedFalse(Sort id);

    List<Comment> findCommentsByUserIdAndIsDeletedFalse(Long userId);
}
