
package com.ajay.anime_app.rest;

import com.ajay.anime_app.service.LikeService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeResource {

    private final LikeService likeService;

    public LikeResource(final LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Long> getLikesByPostId(
            @PathVariable(name = "postId") Long postId) {

        return ResponseEntity.ok(likeService.getLikesByPostId(postId));
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<String> likeThePost(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,
            @PathVariable(name = "postId") Long postId) {

        return ResponseEntity.ok(likeService.likeOrDislikePost(postId, token));
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<Long> getLikesByCommentId(
            @PathVariable(name = "commentId") Long commentId) {

        return ResponseEntity.ok(likeService.getLikesByCommentId(commentId));
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> likeTheComment(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,
            @PathVariable(name = "commentId") Long commentId) {

        return ResponseEntity.ok(likeService.likeOrDislikeComment(commentId, token));
    }
}
