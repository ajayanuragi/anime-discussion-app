package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Like;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.LikeRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final JwtService jwtService;

    public LikeService(final PostRepository postRepository, final UserRepository userRepository, final CommentRepository commentRepository, final LikeRepository likeRepository, final JwtService jwtService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.jwtService = jwtService;
    }

    public Long getLikesByPostId(Long postId) {
        Post post = getPostByPostId(postId);
        return post.getLikeCount();
    }

    public String likeOrDislikePost(Long postId, String token) {
        String username = jwtService.extractUsername(token.substring(7));
        User user = getUserByUsername(username);
        Post post = getPostByPostId(postId);
        Optional<Like> optionalLike = likeRepository.getByPostIdAndUserId(postId, user.getId());
        if (optionalLike.isEmpty()) {
            createNewLike(postId, user);
            incrementPostLikeCount(post);
            return "Liked";
        }
        Like like = optionalLike.get();
        toggleLikeStatus(like, post);
        return like.isDeleted() ? "Disliked" : "Liked";
    }

    private void incrementPostLikeCount(Post post) {
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void createNewLike(Long postId, User user) {
        Like newLike = new Like(postId, user.getId(), 0L, false);
        likeRepository.save(newLike);
    }

    private void toggleLikeStatus(Like like, Post post) {
        boolean previouslyDeleted = like.isDeleted();
        like.setDeleted(!previouslyDeleted);
        likeRepository.save(like);
        if (previouslyDeleted) {
            incrementPostLikeCount(post);
        } else {
            decrementPostLikeCount(post);
        }
    }

    private void decrementPostLikeCount(Post post) {
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
    }

    private Post getPostByPostId(Long postId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId);
        if (post == null) {
            throw new NotFoundException("No post found with postId: " + postId);
        }
        return post;
    }

    public String likeOrDislikeComment(Long commentId, String token) {
        String username = jwtService.extractUsername(token.substring(7));
        User user = getUserByUsername(username);
        Comment comment = getCommentByCommentId(commentId);

        Optional<Like> optionalLike = likeRepository.getByCommentIdAndUserId(commentId, user.getId());

        if (optionalLike.isEmpty()) {
            createNewCommentLike(commentId, user);
            incrementCommentLikeCount(comment);
            return "Liked";
        }

        Like like = optionalLike.get();
        toggleCommentLikeStatus(like, comment);

        return like.isDeleted() ? "Disliked" : "Liked";
    }

    private void createNewCommentLike(Long commentId, User user) {
        Like newCommentLike = new Like(0L, user.getId(), commentId, false);
        likeRepository.save(newCommentLike);
    }

    private void toggleCommentLikeStatus(Like like, Comment comment) {
        boolean previouslyDeleted = like.isDeleted();
        like.setDeleted(!previouslyDeleted);
        likeRepository.save(like);
        if (previouslyDeleted) {
            incrementCommentLikeCount(comment);
        } else {
            decrementCommentLikeCount(comment);
        }
    }

    private void incrementCommentLikeCount(Comment comment) {
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    private void decrementCommentLikeCount(Comment comment) {
        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        commentRepository.save(comment);
    }

    private Comment getCommentByCommentId(Long commentId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId);
        if (comment == null) {
            throw new NotFoundException("No Comment found with commentId: " + commentId);
        }
        return comment;
    }

    public Long getLikesByCommentId(Long commentId) {
        Comment comment = getCommentByCommentId(commentId);
        return comment.getLikeCount();
    }
}

