package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.PostDTO;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PostService postService;

    @Test
    void createShouldSavePostWithCorrectUser() {
        PostDTO request = new PostDTO();
        request.setTitle("Naruto");
        request.setContent("My favorite anime!");

        User user = new User();
        user.setUsername("tester");

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsernameAndIsDeletedFalse("tester"))
                .thenReturn(Optional.of(user));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    post.setId(1L);
                    return post;
                });

        Long postId = postService.create(
                request, "Bearer test-token"
        );

        assertEquals(1L, postId);

        verify(postRepository).save(argThat(post ->
                "Naruto".equals(post.getTitle())
                        && "My favorite anime!".equals(post.getContent())
                        && post.getUser() == user
        ));
    }

    @Test
    void createShouldFailWhenUserNotFound() {
        PostDTO request = new PostDTO();
        request.setTitle("Naruto");
        request.setContent("My favorite anime!");

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(userRepository.findByUsernameAndIsDeletedFalse("tester"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> postService.create(request, "Bearer test-token")
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void getShouldReturnPostWhenFound() {
        Long postId = 1L;

        User user = new User();
        user.setId(10L);

        Post post = new Post();
        post.setId(postId);
        post.setTitle("Naruto");
        post.setContent("Great anime!");
        post.setUser(user);

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        PostDTO result = postService.get(postId);

        assertEquals(postId, result.getId());
        assertEquals("Naruto", result.getTitle());
        assertEquals("Great anime!", result.getContent());
        assertEquals(10L, result.getUserId());

        verify(postRepository).findByIdAndIsDeletedFalse(postId);
    }

    @Test
    void getShouldFailWhenPostNotFound() {
        Long postId = 999L;

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.get(postId)
        );

        assertEquals(
                "No post found with postId: 999",
                exception.getMessage()
        );

        verify(postRepository).findByIdAndIsDeletedFalse(postId);
    }

    @Test
    void updateShouldModifyPostWhenOwner() {
        Long postId = 1L;

        User user = new User();
        user.setUsername("tester");

        Post post = new Post();
        post.setUser(user);
        post.setTitle("Old title");

        PostDTO request = new PostDTO();
        request.setTitle("Updated title");
        request.setContent("Updated content");

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        postService.update(postId, request, "Bearer test-token");

        assertEquals("Updated title", post.getTitle());
        assertEquals("Updated content", post.getContent());

        verify(postRepository).save(post);
    }

    @Test
    void updateShouldFailWhenUserIsNotOwner() {
        Long postId = 1L;

        User owner = new User();
        owner.setUsername("owner");

        Post post = new Post();
        post.setUser(owner);
        post.setTitle("Original title");

        PostDTO request = new PostDTO();
        request.setTitle("Unauthorized update");

        when(jwtService.extractUsername("test-token"))
                .thenReturn("other-user");

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.update(
                        postId, request, "Bearer test-token"
                )
        );

        assertEquals(
                "You can't update this post",
                exception.getMessage()
        );

        assertEquals("Original title", post.getTitle());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updateShouldFailWhenPostNotFound() {
        Long postId = 999L;

        PostDTO request = new PostDTO();
        request.setTitle("Updated title");
        request.setContent("Updated content");

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.update(
                        postId, request, "Bearer test-token"
                )
        );

        assertEquals(
                "No post found with postId: 999",
                exception.getMessage()
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deleteShouldSoftDeletePostWhenOwner() {
        Long postId = 1L;

        User user = new User();
        user.setUsername("tester");

        Post post = new Post();
        post.setUser(user);
        post.setDeleted(false);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        postService.delete(postId, "Bearer test-token");

        assertTrue(post.isDeleted());

        verify(postRepository).save(post);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void deleteShouldFailWhenUserIsNotOwner() {
        Long postId = 1L;

        User owner = new User();
        owner.setUsername("owner");

        Post post = new Post();
        post.setUser(owner);
        post.setDeleted(false);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("other-user");

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(post);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.delete(postId, "Bearer test-token")
        );

        assertEquals(
                "You can't update this post",
                exception.getMessage()
        );

        assertFalse(post.isDeleted());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deleteShouldFailWhenPostNotFound() {
        Long postId = 999L;

        when(jwtService.extractUsername("test-token"))
                .thenReturn("tester");

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.delete(postId, "Bearer test-token")
        );

        assertEquals(
                "No post found with postId: 999",
                exception.getMessage()
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void findAllShouldReturnActivePosts() {
        User user = new User();
        user.setId(10L);

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Naruto");
        post.setContent("Great anime!");
        post.setUser(user);

        when(postRepository.findByIsDeletedFalse(Sort.by("id")))
                .thenReturn(List.of(post));

        List<PostDTO> result = postService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals("Naruto", result.getFirst().getTitle());
        assertEquals(10L, result.getFirst().getUserId());

        verify(postRepository).findByIsDeletedFalse(Sort.by("id"));
    }

    @Test
    void getPostsByUserIdShouldReturnUserPosts() {
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Naruto");
        post.setContent("Great anime!");
        post.setUser(user);

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(user);

        when(postRepository.findByUserId(userId))
                .thenReturn(List.of(post));

        List<PostDTO> result = postService.getPostsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals("Naruto", result.getFirst().getTitle());
        assertEquals(userId, result.getFirst().getUserId());

        verify(postRepository).findByUserId(userId);
    }

    @Test
    void getPostsByUserIdShouldFailWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.getPostsByUserId(userId)
        );

        assertEquals(
                "No user found with userId: 999",
                exception.getMessage()
        );

        verify(postRepository, never()).findByUserId(anyLong());
    }
}