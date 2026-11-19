package com.ajay.anime_app.service;

import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.CommentDTO;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import com.ajay.anime_app.util.NotFoundException;

import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createShouldSaveComment() {
        CommentDTO request = new CommentDTO();
        request.setContent("Great anime!");
        request.setPost(1L);

        User user = new User();
        user.setUsername("testuser");

        Post post = new Post();

        when(jwtService.extractUsername("test-token"))
                .thenReturn("testuser");

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    comment.setId(10L);
                    return comment;
                });

        Long commentId = commentService.create(
                request, null, "Bearer test-token"
        );

        assertEquals(10L, commentId);

        verify(commentRepository).save(argThat(comment ->
                "Great anime!".equals(comment.getContent())
                        && comment.getUser() == user
                        && comment.getPost() == post
        ));
    }
    @Test
    void createShouldFailWhenPostNotFound() {
        CommentDTO request = new CommentDTO();
        request.setContent("Great anime!");
        request.setPost(999L);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("testuser");

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(new User()));

        when(postRepository.findById(999L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> commentService.create(
                        request, null, "Bearer test-token"
                )
        );

        assertEquals("post not found", exception.getMessage());

        verify(commentRepository, never()).save(any(Comment.class));
    }
    @Test
    void updateShouldModifyCommentWhenOwner() {
        Long commentId = 1L;

        User user = new User();
        user.setUsername("testuser");

        Comment comment = new Comment();
        comment.setUser(user);

        CommentDTO request = new CommentDTO();
        request.setContent("Updated comment");

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("testuser");

        commentService.update(
                commentId, request, "Bearer test-token"
        );

        assertEquals("Updated comment", comment.getContent());

        verify(commentRepository).save(comment);
    }
    @Test
    void updateShouldFailWhenUserIsNotOwner() {
        Long commentId = 1L;

        User owner = new User();
        owner.setUsername("owner");

        Comment comment = new Comment();
        comment.setUser(owner);

        CommentDTO request = new CommentDTO();
        request.setContent("Unauthorized update");

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("otheruser");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> commentService.update(
                        commentId, request, "Bearer test-token"
                )
        );

        assertEquals(
                "You can't modify someone else comment.",
                exception.getMessage()
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }
    @Test
    void updateShouldFailWhenCommentNotFound() {
        Long commentId = 999L;

        CommentDTO request = new CommentDTO();
        request.setContent("Updated comment");

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(null);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("testuser");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> commentService.update(
                        commentId, request, "Bearer test-token"
                )
        );

        assertEquals(
                "No comment with commentId: 999found",
                exception.getMessage()
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteShouldSoftDeleteCommentWhenOwner() {
        Long commentId = 1L;

        User user = new User();
        user.setUsername("testuser");

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setDeleted(false);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("testuser");

        commentService.delete(commentId, "Bearer test-token");

        assertTrue(comment.isDeleted());

        verify(commentRepository).save(comment);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteShouldFailWhenUserIsNotOwner() {
        Long commentId = 1L;

        User owner = new User();
        owner.setUsername("owner");

        Comment comment = new Comment();
        comment.setUser(owner);
        comment.setDeleted(false);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(comment);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("otheruser");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> commentService.delete(
                        commentId, "Bearer test-token"
                )
        );

        assertEquals(
                "You can't modify someone else comment.",
                exception.getMessage()
        );

        assertFalse(comment.isDeleted());

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteShouldFailWhenCommentNotFound() {
        Long commentId = 999L;

        when(commentRepository.findByIdAndIsDeletedFalse(commentId))
                .thenReturn(null);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("testuser");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> commentService.delete(
                        commentId, "Bearer test-token"
                )
        );

        assertEquals(
                "No comment with commentId: 999found",
                exception.getMessage()
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }
}