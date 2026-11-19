package com.ajay.anime_app.rest;

import com.ajay.anime_app.model.UserUsernameUnique;
import com.ajay.anime_app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.ajay.anime_app.model.UserDTO;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserResource userResource;

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
                .standaloneSetup(userResource)
                .setValidator(validator)
                .build();
    }

    @Test
    void getAllUsersShouldReturn200() throws Exception {
        UserDTO user = new UserDTO();
        user.setUsername("ajay");

        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ajay"));

        verify(userService).findAll();
    }

    @Test
    void getUserShouldReturn200() throws Exception {
        UserDTO user = new UserDTO();
        user.setUsername("ajay");

        when(userService.get(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ajay"));

        verify(userService).get(1L);
    }

    @Test
    void updateUserShouldReturn200() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "ajay",
                                "password": "password123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(userService).update(
                eq(1L),
                any(UserDTO.class),
                eq("Bearer test-token")
        );
    }

    @Test
    void deleteUserShouldReturn200() throws Exception {
        when(userService.getReferencedWarning(1L)).thenReturn(null);

        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "User with userId1deleted successfully"
                ));

        verify(userService).getReferencedWarning(1L);
        verify(userService).delete(1L, "Bearer test-token");
    }
}