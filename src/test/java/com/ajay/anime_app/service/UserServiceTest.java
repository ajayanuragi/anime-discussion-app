package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.UserDTO;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import com.ajay.anime_app.util.ReferencedWarning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findAllShouldReturnActiveUsers() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("johndoe");

        when(userRepository.findByIsDeletedFalse(Sort.by("id")))
                .thenReturn(List.of(user));

        List<UserDTO> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals("John", result.getFirst().getFirstName());
        assertEquals("Doe", result.getFirst().getLastName());
        assertEquals("johndoe", result.getFirst().getUsername());

        verify(userRepository).findByIsDeletedFalse(Sort.by("id"));
    }

    @Test
    void getShouldReturnUserWhenFound() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("johndoe");

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(user);

        UserDTO result = userService.get(userId);

        assertEquals(userId, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("johndoe", result.getUsername());

        verify(userRepository).findByIdAndIsDeletedFalse(userId);
    }

    @Test
    void getShouldFailWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.get(userId)
        );

        assertEquals(
                "User not found with userId: 999",
                exception.getMessage()
        );

        verify(userRepository).findByIdAndIsDeletedFalse(userId);
    }

    @Test
    void updateShouldModifyUserWhenOwner() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");

        UserDTO request = new UserDTO();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUsername("johndoe");
        request.setPassword("newPassword123");

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(user);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("johndoe");

        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("encodedPassword");

        userService.update(userId, request, "Bearer test-token");

        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
    }

    @Test
    void updateShouldFailWhenUserIsNotOwner() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");
        user.setFirstName("John");

        UserDTO request = new UserDTO();
        request.setFirstName("Hacker");
        request.setUsername("johndoe");
        request.setPassword("newPassword123");

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(user);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("otheruser");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(
                        userId, request, "Bearer test-token"
                )
        );

        assertEquals(
                "You can't modify this user",
                exception.getMessage()
        );

        assertEquals("John", user.getFirstName());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateShouldFailWhenUserNotFound() {
        Long userId = 999L;

        UserDTO request = new UserDTO();
        request.setFirstName("John");
        request.setUsername("johndoe");

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(null);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("johndoe");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(
                        userId, request, "Bearer test-token"
                )
        );

        assertEquals(
                "User not found with userId: 999",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deleteShouldSoftDeleteUserWhenOwner() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");
        user.setDeleted(false);

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(user);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("johndoe");

        userService.delete(userId, "Bearer test-token");

        assertTrue(user.isDeleted());

        verify(userRepository).save(user);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteShouldFailWhenUserIsNotOwner() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");
        user.setDeleted(false);

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(user);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("otheruser");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.delete(userId, "Bearer test-token")
        );

        assertEquals(
                "You can't modify this user",
                exception.getMessage()
        );

        assertFalse(user.isDeleted());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteShouldFailWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findByIdAndIsDeletedFalse(userId))
                .thenReturn(null);

        when(jwtService.extractUsername("test-token"))
                .thenReturn("johndoe");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.delete(userId, "Bearer test-token")
        );

        assertEquals(
                "User not found with userId: 999",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void userNameExistsShouldReturnTrueWhenUsernameExists() {
        String username = "johndoe";

        when(userRepository.existsByUsernameIgnoreCase(username))
                .thenReturn(true);

        boolean result = userService.userNameExists(username);

        assertTrue(result);

        verify(userRepository).existsByUsernameIgnoreCase(username);
    }

    @Test
    void userNameExistsShouldReturnFalseWhenUsernameDoesNotExist() {
        String username = "newuser";

        when(userRepository.existsByUsernameIgnoreCase(username))
                .thenReturn(false);

        boolean result = userService.userNameExists(username);

        assertFalse(result);

        verify(userRepository).existsByUsernameIgnoreCase(username);
    }

    @Test
    void getReferencedWarningShouldReturnWarningWhenUserHasPost() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Post post = new Post();
        post.setId(10L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(postRepository.findFirstByUser(user))
                .thenReturn(post);

        ReferencedWarning result = userService.getReferencedWarning(userId);

        assertNotNull(result);
        assertEquals("user.post.user.referenced", result.getKey());

        verify(postRepository).findFirstByUser(user);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void getReferencedWarningShouldReturnWarningWhenUserHasComment() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Comment comment = new Comment();
        comment.setId(20L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(postRepository.findFirstByUser(user))
                .thenReturn(null);

        when(commentRepository.findFirstByUser(user))
                .thenReturn(comment);

        ReferencedWarning result = userService.getReferencedWarning(userId);

        assertNotNull(result);
        assertEquals("user.comment.user.referenced", result.getKey());

        verify(commentRepository).findFirstByUser(user);
    }

    @Test
    void getReferencedWarningShouldReturnNullWhenNoReferences() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ReferencedWarning result =
                userService.getReferencedWarning(userId);

        assertNull(result);

        verify(postRepository).findFirstByUser(user);
        verify(commentRepository).findFirstByUser(user);
    }

    @Test
    void getReferencedWarningShouldFailWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.getReferencedWarning(userId)
        );

        verifyNoInteractions(postRepository, commentRepository);
    }

    @Test
    void searchUsersShouldReturnMatchingUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("rahul123");

        when(userRepository.findByUsernameContainingIgnoreCaseAndIsDeletedFalse("rah"))
                .thenReturn(List.of(user));

        List<UserDTO> result = userService.searchUsers("rah");

        assertEquals(1, result.size());
        assertEquals("rahul123", result.getFirst().getUsername());
        assertNull(result.getFirst().getPassword());

        verify(userRepository)
                .findByUsernameContainingIgnoreCaseAndIsDeletedFalse("rah");
    }
}