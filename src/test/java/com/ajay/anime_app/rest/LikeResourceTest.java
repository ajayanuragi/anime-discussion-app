package com.ajay.anime_app.rest;

import com.ajay.anime_app.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikeResourceTest {

    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeResource likeResource;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(likeResource)
                .build();
    }

    @Test
    void getLikesByPostIdShouldReturn200() throws Exception {
        when(likeService.getLikesByPostId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/likes/post/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(likeService).getLikesByPostId(1L);
    }

    @Test
    void likeThePostShouldReturn200() throws Exception {
        when(likeService.likeOrDislikePost(1L, "Bearer test-token"))
                .thenReturn("Post liked successfully");

        mockMvc.perform(post("/api/likes/post/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post liked successfully"));

        verify(likeService).likeOrDislikePost(1L, "Bearer test-token");
    }

    @Test
    void getLikesByCommentIdShouldReturn200() throws Exception {
        when(likeService.getLikesByCommentId(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/likes/comment/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(likeService).getLikesByCommentId(1L);
    }

    @Test
    void likeTheCommentShouldReturn200() throws Exception {
        when(likeService.likeOrDislikeComment(1L, "Bearer test-token"))
                .thenReturn("Comment liked successfully");

        mockMvc.perform(post("/api/likes/comment/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment liked successfully"));

        verify(likeService).likeOrDislikeComment(1L, "Bearer test-token");
    }
}