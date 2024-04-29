package com.ajay.anime_app.rest;

import com.ajay.anime_app.model.PostDTO;
import com.ajay.anime_app.service.PostService;
import com.ajay.anime_app.util.ReferencedException;
import com.ajay.anime_app.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostResource {

    private final PostService postService;

    public PostResource(PostService postService) {
        this.postService = postService;

    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(postService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createPost(@RequestHeader("Authorization") String token, @RequestBody @Valid final PostDTO postDTO) {
        final Long createdId = postService.create(postDTO, token);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@RequestHeader("Authorization") String token, @PathVariable(name = "id") final Long id,
                                             @RequestBody @Valid final PostDTO postDTO) {
        postService.update(id, postDTO, token);
        return ResponseEntity.ok("Post with postId: " + id + " update successfully");
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<String> deletePost(@RequestHeader("Authorization") String token, @PathVariable(name = "id") final Long id) {
        final ReferencedWarning referencedWarning = postService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        postService.delete(id, token);
        return ResponseEntity.ok("Post with postId: " + id + " update successfully");
    }

    @GetMapping("/user/{userId}")
    public List<PostDTO> getPostsByUserId(@PathVariable Long userId) {
        return postService.getPostsByUserId(userId);
    }

}
