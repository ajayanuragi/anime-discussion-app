package com.ajay.anime_app.rest;

import com.ajay.anime_app.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.ajay.anime_app.model.CommentDTO;

import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(MockitoExtension.class)
class CommentResourceTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentResource commentResource;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentResource)
                .build();
    }

    @Test
    void getAllCommentsShouldReturn200() throws Exception {
        when(commentService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(commentService).findAll();
    }

    @Test
    void getCommentShouldReturn200() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);

        when(commentService.get(1L)).thenReturn(commentDTO);

        mockMvc.perform(get("/api/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(commentService).get(1L);
    }

    @Test
    void createCommentShouldReturn201() throws Exception {
        when(commentService.create(
                any(CommentDTO.class),
                isNull(),
                eq("Bearer test-token")
        )).thenReturn(1L);

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "content": "Great anime!"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));

        verify(commentService).create(
                any(CommentDTO.class),
                isNull(),
                eq("Bearer test-token")
        );
    }

    @Test
    void createReplyShouldReturn201() throws Exception {
        when(commentService.create(
                any(CommentDTO.class),
                eq(5L),
                eq("Bearer test-token")
        )).thenReturn(2L);

        mockMvc.perform(post("/api/comments")
                        .param("parentId", "5")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "content": "I agree!"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(content().string("2"));

        verify(commentService).create(
                any(CommentDTO.class),
                eq(5L),
                eq("Bearer test-token")
        );
    }

    @Test
    void updateCommentShouldReturn200() throws Exception {
        mockMvc.perform(put("/api/comments/1")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "content": "Updated comment"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(commentService).update(
                eq(1L),
                any(CommentDTO.class),
                eq("Bearer test-token")
        );
    }

    @Test
    void deleteCommentShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/comments/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(commentService).delete(1L, "Bearer test-token");
    }
    @Test
    void getCommentsByUserIdShouldReturn200() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);

        when(commentService.getCommentsByUserId(2L))
                .thenReturn(List.of(commentDTO));

        mockMvc.perform(get("/api/comments/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(commentService).getCommentsByUserId(2L);
    }

    @Test
    void getCommentsByPostIdShouldReturn200() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);

        when(commentService.getCommentsByPostId(5L))
                .thenReturn(List.of(commentDTO));

        mockMvc.perform(get("/api/comments/post/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(commentService).getCommentsByPostId(5L);
    }
    @Test
    void getCommentsByParentIdShouldReturn200() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(2L);

        when(commentService.getCommentsByParentId(1L))
                .thenReturn(List.of(commentDTO));

        mockMvc.perform(get("/api/comments/parent/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));

        verify(commentService).getCommentsByParentId(1L);
    }
}
