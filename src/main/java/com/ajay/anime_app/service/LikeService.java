package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Like;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.LikeRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final JwtService jwtService;

    public LikeService(final PostRepository postRepository, final UserRepository userRepository, final CommentRepository commentRepository, final LikeRepository likeRepository, JwtService jwtService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.jwtService = jwtService;
    }

    public Long getLikesByPostId(Long postId) {
        Post post = getPostByPostId(postId);
        return post.getLikeCount();
    }

    public String likeOrDislikePost(Long postId) {
        Post post = getPostByPostId(postId);
        Optional<Like> optionalLike = likeRepository.getByPostId(postId);

        boolean isLiked = false;
        if (optionalLike.isPresent()) {
            isLiked = true;
            System.out.println(optionalLike.get().getId());
        }

        long userId = post.getUser().getId();
        if (!isLiked) {
            Like like = new Like(userId, postId, 0L, false);
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
            likeRepository.save(like);
            return "liked";
        } else {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            likeRepository.deleteByUserIdAndPostId(userId, postId);
            return "disliked";
        }
    }

    private Post getPostByPostId(Long postId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId);
        if (post == null) {
            throw new NotFoundException("No post found with postId: " + postId);
        }
        return post;
    }

//    private boolean checkIfAlreadyLiked(Long postId) {
//        boolean isLiked = false;
//        Optional<Like> optionalLike = likeRepository.findByPostId(postId);
//        if (optionalLike.isEmpty()) {
//            return isLiked;
//        }
//        return true;
//    }
}

