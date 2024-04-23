package com.ajay.anime_app.rest;

import com.ajay.anime_app.service.LikeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/likes")
public class LikeResource {

    private final LikeService likeService;

    public LikeResource(final LikeService likeService) {
        this.likeService = likeService;
    }

//
//    @GetMapping("/post/{postId}")
//    public ResponseEntity<Long> getLikesByPostId(@PathVariable(name = "postId") Long postId){
//        Long likeCount = likeService.getLikesByPostId(postId);
//        return ResponseEntity.ok(likeCount);
//    }

}
