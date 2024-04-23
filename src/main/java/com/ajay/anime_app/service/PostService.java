package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Like;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.PostDTO;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.LikeRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import com.ajay.anime_app.util.ReferencedWarning;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostService(final PostRepository postRepository, final UserRepository userRepository,
                       final CommentRepository commentRepository, final LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    public List<PostDTO> findAll() {
        final List<Post> posts = postRepository.findByIsDeletedFalse(Sort.by("id"));
        return posts.stream()
                .map(post -> mapToDTO(post, new PostDTO()))
                .toList();
    }

    public PostDTO get(final Long id) {
        Post post = postRepository.findByIdAndIsDeletedFalse(id);
        if (post == null) {
            throw new NotFoundException("No post found with postId: " + id);
        }
        return mapToDTO(post, new PostDTO());

    }

    public Long create(final PostDTO postDTO) {
        final Post post = new Post();
        validatePostDetails(postDTO);
        mapToEntity(postDTO, post);
        return postRepository.save(post).getId();
    }

    private void validatePostDetails(PostDTO postDTO) {
        if (postDTO.getUser() == null || postDTO.getTitle() == null) {
            throw new NotFoundException("Please provide user details and title of the post");
        }
    }

    public void update(final Long id, final PostDTO postDTO) {
        final Post post = postRepository.findByIdAndIsDeletedFalse(id);
        if (post == null) {
            throw new NotFoundException("No post found with postId: " + id);
        }
        mapToEntity(postDTO, post);
        postRepository.save(post);
    }

    public void delete(final Long id) {

        final Post post = postRepository.findByIdAndIsDeletedFalse(id);
        if (post == null) {
            throw new NotFoundException("No post found with postId: " + id);
        }
        post.setDeleted(true);
        postRepository.save(post);
    }

    private PostDTO mapToDTO(final Post post, final PostDTO postDTO) {
        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setUser(post.getUser() == null ? null : post.getUser().getId());
        postDTO.setLikeCount(post.getLikeCount());
        return postDTO;
    }

    private Post mapToEntity(final PostDTO postDTO, final Post post) {
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        final User user = postDTO.getUser() == null ? null : userRepository.findById(postDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        post.setUser(user);
        return post;
    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Post post = postRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Comment postComment = commentRepository.findFirstByPost(post);
        if (postComment != null) {
            referencedWarning.setKey("post.comment.post.referenced");
            referencedWarning.addParam(postComment.getId());
            return referencedWarning;
        }
        return null;
    }

    public List<PostDTO> getPostsByUserId(Long userId) {
        checkIfUserExists(userId);
        List<Post> posts = postRepository.findByUserId(userId);
        return posts.stream()
                .map(post -> mapToDTO(post, new PostDTO()))
                .toList();
    }

    private void checkIfUserExists(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No user found with userId: " + userId);
        }
    }

    @Transactional
    public String likeOrDislikePost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        PostDTO postDTO = get(postId);
        if (optionalPost.isPresent()) {
            boolean isLiked = checkIfAlreadyLiked(postId);
            Post post = optionalPost.get();
            long userId = post.getUser().getId();
            if (!isLiked) {
                Like like = new Like(userId, postId, 0L, false);
                post.setLikeCount(post.getLikeCount() + 1);
                postDTO.setLikeCount(post.getLikeCount());
                update(postId, postDTO);
                likeRepository.save(like);
                return "liked";
            } else {
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                likeRepository.deleteByUserIdAndPostId(userId, postId);
                return "disliked";
            }
        } else {
            throw new NotFoundException("Post not found with id : " + postId);
        }
    }

    private boolean checkIfAlreadyLiked(Long postId) {
        boolean isLiked = false;
        Optional<Like> optionalLike = likeRepository.findByPostId(postId);
        if (optionalLike.isEmpty()) {
            return isLiked;
        }
        return true;
    }
}
