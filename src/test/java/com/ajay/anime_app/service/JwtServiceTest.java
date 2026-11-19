package com.ajay.anime_app.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import com.ajay.anime_app.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private JwtService jwtService;


    @Test
    void generateTokenShouldReturnJwt() {
        User user = new User();
        user.setUsername("testuser");

        Jwt jwt = mock(Jwt.class);

        when(jwt.getTokenValue()).thenReturn("generated-token");

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        String token = jwtService.generateToken(user);

        assertEquals("generated-token", token);

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void extractUsernameShouldReturnUsername() {
        String token = "test-token";

        Jwt jwt = mock(Jwt.class);

        when(jwtDecoder.decode(token)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("testuser");

        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);

        verify(jwtDecoder).decode(token);
    }
}