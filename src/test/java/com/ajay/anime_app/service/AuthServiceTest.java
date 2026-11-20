package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.AuthenticationResponse;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.UserDTO;
import com.ajay.anime_app.repos.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldAssignUserRole() {
        UserDTO request = new UserDTO();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                "USER".equals(user.getRole())
                        && "encodedPassword".equals(user.getPassword())
        ));
    }

    @Test
    void authenticateShouldReturnToken() {
        UserDTO request = new UserDTO();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        AuthenticationResponse response = authService.authenticate(request);

        assertNotNull(response);
        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(jwtService).generateToken(user);
    }

    @Test
    void authenticateShouldFailForInvalidCredentials() {
        UserDTO request = new UserDTO();
        request.setUsername("testuser");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(request)
        );

        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void registerShouldGenerateJwtToken() {
        UserDTO request = new UserDTO();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("test-jwt-token");

        AuthenticationResponse response = authService.register(request);

        assertNotNull(response);
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void registerShouldSaveUserDetails() {
        UserDTO request = new UserDTO();
        request.setFirstName("Rahul");
        request.setLastName("Sharma");
        request.setUsername("rahul123");
        request.setPassword("password123");

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                "Rahul".equals(user.getFirstName())
                        && "Sharma".equals(user.getLastName())
                        && "rahul123".equals(user.getUsername())
                        && "encodedPassword".equals(user.getPassword())
        ));
    }

    @Test
    void authenticateShouldFailWhenUserNotFound() {
        UserDTO request = new UserDTO();
        request.setUsername("unknownuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("unknownuser"))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> authService.authenticate(request)
        );

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(jwtService, never()).generateToken(any(User.class));
    }
}