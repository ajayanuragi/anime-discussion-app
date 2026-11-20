package com.ajay.anime_app.rest;

import com.ajay.anime_app.model.PostDTO;
import com.ajay.anime_app.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostResourceTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostResource postResource;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(postResource)
                .build();
    }

    @Test
    void getAllPostsShouldReturn200() throws Exception {
        when(postService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(postService).findAll();
    }

    @Test
    void getPostShouldReturn200() throws Exception {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);

        when(postService.get(1L)).thenReturn(postDTO);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(postService).get(1L);
    }

    @Test
    void createPostShouldReturn201() throws Exception {
        when(postService.create(any(PostDTO.class), eq("Bearer test-token")))
                .thenReturn(1L);

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Naruto",
                                    "content": "Discussion about Naruto"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));

        verify(postService).create(
                any(PostDTO.class),
                eq("Bearer test-token")
        );
    }

    @Test
    void updatePostShouldReturn200() throws Exception {
        mockMvc.perform(put("/api/posts/1")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated Naruto",
                                    "content": "Updated discussion about Naruto"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Post with postId: 1 update successfully"
                ));

        verify(postService).update(
                eq(1L),
                any(PostDTO.class),
                eq("Bearer test-token")
        );
    }

    @Test
    void deletePostShouldReturn200() throws Exception {
        when(postService.getReferencedWarning(1L))
                .thenReturn(null);

        mockMvc.perform(delete("/api/posts/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Post with postId: 1 update successfully"
                ));

        verify(postService).getReferencedWarning(1L);
        verify(postService).delete(1L, "Bearer test-token");
    }

    @Test
    void getPostsByUserIdShouldReturn200() throws Exception {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);

        when(postService.getPostsByUserId(2L))
                .thenReturn(List.of(postDTO));

        mockMvc.perform(get("/api/posts/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(postService).getPostsByUserId(2L);
    }
}