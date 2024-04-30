package com.ajay.anime_app.rest;

import com.ajay.anime_app.model.CommentDTO;
import com.ajay.anime_app.service.CommentService;
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
@RequestMapping(value = "/api/comments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentResource {

    private final CommentService commentService;

    public CommentResource(final CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        return ResponseEntity.ok(commentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(commentService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createComment(@RequestHeader("Authorization") String token, @RequestBody @Valid final CommentDTO commentDTO, @RequestParam(required = false) Long parentId) {
        final Long createdId = commentService.create(commentDTO, parentId, token);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateComment(@RequestHeader("Authorization") String token, @PathVariable(name = "id") final Long id,
                                              @RequestBody @Valid final CommentDTO commentDTO) {
        commentService.update(id, commentDTO, token);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteComment(@RequestHeader("Authorization") String token, @PathVariable(name = "id") final Long id) {
        final ReferencedWarning referencedWarning = commentService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        commentService.delete(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getCommentsByParentId(parentId));
    }

}
