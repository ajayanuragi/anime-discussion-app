package com.ajay.anime_app.service;

import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.LikeRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public LikeService(final PostRepository postRepository, final UserRepository userRepository, final CommentRepository commentRepository, final LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

//    public Long getLikesByPostId(Long postId) {
//
//
//
//        return likeCount;
//    }
}
