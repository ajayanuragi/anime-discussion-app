package com.ajay.anime_app.rest;

import com.ajay.anime_app.service.AuthService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.junit.jupiter.api.BeforeEach;
import com.ajay.anime_app.domain.AuthenticationResponse;
import com.ajay.anime_app.model.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.ajay.anime_app.model.UserUsernameUnique;
import com.ajay.anime_app.service.UserService;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;
    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AutowireCapableBeanFactory beanFactory =
                mock(AutowireCapableBeanFactory.class);

       lenient().when(beanFactory.createBean(ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    Class<?> validatorClass = invocation.getArgument(0);

                    if (validatorClass.equals(
                            UserUsernameUnique.UserUsernameUniqueValidator.class)) {
                        return new UserUsernameUnique.UserUsernameUniqueValidator(
                                userService,
                                new MockHttpServletRequest()
                        );
                    }

                    return validatorClass.getDeclaredConstructor().newInstance();
                });

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setConstraintValidatorFactory(
                new SpringConstraintValidatorFactory(beanFactory)
        );
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setValidator(validator)
                .build();
    }

    @Test
    void registerShouldReturn200() throws Exception {
        when(authService.register(any(UserDTO.class)))
                .thenReturn(new AuthenticationResponse("test-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "firstName": "Ajay",
                                "lastName": "Kumar",
                                "username": "testuser",
                                "password": "Password123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));

        verify(authService).register(any(UserDTO.class));
    }

    @Test
    void loginShouldReturn200() throws Exception {
        when(authService.authenticate(any(UserDTO.class)))
                .thenReturn(new AuthenticationResponse("login-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "password": "Password123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));

        verify(authService).authenticate(any(UserDTO.class));
    }

    @Test
    void registerShouldReturn400WhenUsernameIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "firstName": "Ajay",
                                "lastName": "Kumar",
                                "username": "",
                                "password": "Password123"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
    @Test
    void registerShouldReturn400WhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "firstName": "Ajay",
                                "lastName": "Kumar",
                                "username": "testuser",
                                "password": ""
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}