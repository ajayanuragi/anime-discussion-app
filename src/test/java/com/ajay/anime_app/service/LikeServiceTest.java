package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.LikeRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import com.ajay.anime_app.domain.Like;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.ajay.anime_app.util.NotFoundException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LikeService likeService;

    @Test
    void likeOrDislikePostShouldCreateNewLike() {
        Long postId = 1L;

        User user = new User();
        user.setId(10L);

        Post post = new Post();
        post.setId(postId);
        post.setLikeCount(0L);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        when(likeRepository.getByPostIdAndUserId(postId, 10L))
                .thenReturn(Optional.empty());

        String result = likeService.likeOrDislikePost(
                postId, "Bearer test-token"
        );

        assertEquals("Liked", result);
        assertEquals(1L, post.getLikeCount());

        verify(likeRepository).save(argThat(like ->
                like.getPostId().equals(postId)
                        && like.getUserId().equals(10L)
                        && !like.isDeleted()
        ));

        verify(postRepository).save(post);
    }
    @Test
    void likeOrDislikePostShouldUnlikeExistingLike() {
        Long postId = 1L;

        User user = new User();
        user.setId(10L);

        Post post = new Post();
        post.setId(postId);
        post.setLikeCount(1L);

        Like like = new Like(postId, 10L, 0L, false);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        when(likeRepository.getByPostIdAndUserId(postId, 10L))
                .thenReturn(Optional.of(like));

        String result = likeService.likeOrDislikePost(
                postId, "Bearer test-token"
        );

        assertEquals("Disliked", result);
        assertTrue(like.isDeleted());
        assertEquals(0L, post.getLikeCount());

        verify(likeRepository).save(like);
        verify(postRepository).save(post);
    }
    @Test
    void likeOrDislikePostShouldReLikePreviouslyUnlikedPost() {
        Long postId = 1L;

        User user = new User();
        user.setId(10L);

        Post post = new Post();
        post.setId(postId);
        post.setLikeCount(0L);

        Like like = new Like(postId, 10L, 0L, true);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        when(likeRepository.getByPostIdAndUserId(postId, 10L))
                .thenReturn(Optional.of(like));

        String result = likeService.likeOrDislikePost(
                postId, "Bearer test-token"
        );

        assertEquals("Liked", result);
        assertFalse(like.isDeleted());
        assertEquals(1L, post.getLikeCount());

        verify(likeRepository).save(like);
        verify(postRepository).save(post);
    }

    @Test
    void likeOrDislikePostShouldFailWhenPostNotFound() {
        Long postId = 999L;

        User user = new User();
        user.setId(10L);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> likeService.likeOrDislikePost(
                        postId, "Bearer test-token"
                )
        );

        assertEquals(
                "No post found with postId: 999",
                exception.getMessage()
        );

        verify(likeRepository, never()).save(any(Like.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void likeOrDislikePostShouldFailWhenUserNotFound() {
        Long postId = 1L;

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> likeService.likeOrDislikePost(
                        postId, "Bearer test-token"
                )
        );

        assertEquals("User not found", exception.getMessage());

        verify(postRepository, never())
                .findByIdAndIsDeletedFalse(anyLong());

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void getLikesByPostIdShouldReturnLikeCount() {
        Long postId = 1L;

        Post post = new Post();
        post.setId(postId);
        post.setLikeCount(5L);

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        Long result = likeService.getLikesByPostId(postId);

        assertEquals(5L, result);

        verify(postRepository).findByIdAndIsDeletedFalse(postId);
    }

    @Test
    void getLikesByPostIdShouldFailWhenPostNotFound() {
        Long postId = 999L;

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> likeService.getLikesByPostId(postId)
        );

        assertEquals(
                "No post found with postId: 999",
                exception.getMessage()
        );

        verify(postRepository).findByIdAndIsDeletedFalse(postId);
    }
    @Test
    void likeOrDislikeCommentShouldCreateNewLike() {
        Long commentId = 1L;

        User user = new User();
        user.setId(10L);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setLikeCount(0L);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(likeRepository.getByCommentIdAndUserId(commentId, 10L))
                .thenReturn(Optional.empty());

        String result = likeService.likeOrDislikeComment(
                commentId, "Bearer test-token"
        );

        assertEquals("Liked", result);
        assertEquals(1L, comment.getLikeCount());

        verify(likeRepository).save(argThat(like ->
                commentId.equals(like.getCommentId())
                        && Long.valueOf(10L).equals(like.getUserId())
                        && !like.isDeleted()
        ));

        verify(commentRepository).save(comment);
    }

    @Test
    void likeOrDislikeCommentShouldUnlikeExistingLike() {
        Long commentId = 1L;

        User user = new User();
        user.setId(10L);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setLikeCount(1L);

        Like like = new Like(0L, 10L, commentId, false);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(likeRepository.getByCommentIdAndUserId(commentId, 10L))
                .thenReturn(Optional.of(like));

        String result = likeService.likeOrDislikeComment(
                commentId, "Bearer test-token"
        );

        assertEquals("Disliked", result);
        assertTrue(like.isDeleted());
        assertEquals(0L, comment.getLikeCount());

        verify(likeRepository).save(like);
        verify(commentRepository).save(comment);
    }
    @Test
    void likeOrDislikeCommentShouldReLikePreviouslyUnlikedComment() {
        Long commentId = 1L;

        User user = new User();
        user.setId(10L);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setLikeCount(0L);

        Like like = new Like(0L, 10L, commentId, true);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(likeRepository.getByCommentIdAndUserId(commentId, 10L))
                .thenReturn(Optional.of(like));

        String result = likeService.likeOrDislikeComment(
                commentId, "Bearer test-token"
        );

        assertEquals("Liked", result);
        assertFalse(like.isDeleted());
        assertEquals(1L, comment.getLikeCount());

        verify(likeRepository).save(like);
        verify(commentRepository).save(comment);
    }

    @Test
    void likeOrDislikeCommentShouldFailWhenCommentNotFound() {
        Long commentId = 999L;

        User user = new User();
        user.setId(10L);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> likeService.likeOrDislikeComment(
                        commentId, "Bearer test-token"
                )
        );

        assertEquals(
                "No Comment found with commentId: 999",
                exception.getMessage()
        );

        verify(likeRepository, never()).save(any(Like.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void likeOrDislikeCommentShouldFailWhenUserNotFound() {
        Long commentId = 1L;

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsername("tester"))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> likeService.likeOrDislikeComment(
                        commentId, "Bearer test-token"
                )
        );

        assertEquals("User not found", exception.getMessage());

        verify(commentRepository, never())
                .findByIdAndIsDeletedFalse(anyLong());

        verify(likeRepository, never()).save(any(Like.class));
    }
    @Test
    void getLikesByCommentIdShouldReturnLikeCount() {
        Long commentId = 1L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setLikeCount(5L);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        Long result = likeService.getLikesByCommentId(commentId);

        assertEquals(5L, result);

        verify(commentRepository)
                .findByIdAndIsDeletedFalse(commentId);
    }
    @Test
    void getLikesByCommentIdShouldFailWhenCommentNotFound() {
        Long commentId = 999L;

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> likeService.getLikesByCommentId(commentId)
        );

        assertEquals(
                "No Comment found with commentId: 999",
                exception.getMessage()
        );

        verify(commentRepository)
                .findByIdAndIsDeletedFalse(commentId);
    }
}