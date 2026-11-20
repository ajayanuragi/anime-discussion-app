package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.PostDTO;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.LikeRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import com.ajay.anime_app.util.ReferencedWarning;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final JwtService jwtService;
    private final LikeRepository likeRepository;

    public PostService(final PostRepository postRepository, final UserRepository userRepository, final CommentRepository commentRepository, final JwtService jwtService, final LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.jwtService = jwtService;
        this.likeRepository = likeRepository;
    }

    public List<PostDTO> findAll() {
        final List<Post> posts =
                postRepository.findByIsDeletedFalse(Sort.by("id"));

        Long currentUserId = getCurrentUserId();

        return posts.stream()
                .map(post -> mapToDTO(post, new PostDTO(), currentUserId))
                .toList();
    }


    public PostDTO get(final Long id) {
        Post post = postRepository.findByIdAndIsDeletedFalse(id);

        if (post == null) {
            throw new NotFoundException("No post found with postId: " + id);
        }

        Long currentUserId = getCurrentUserId();

        return mapToDTO(post, new PostDTO(), currentUserId);
    }

    public Long create(final PostDTO postDTO, String token) {
        final Post post = new Post();
        String username = getUsernameFromToken(token);
        User user = userRepository
                .findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        post.setUser(user);
        mapToEntity(postDTO, post);
        return postRepository.save(post).getId();
    }

    private String getUsernameFromToken(String token) {
        return jwtService.extractUsername(token.substring(7));
    }

    public void update(final Long id, final PostDTO postDTO, String token) {
        String username = getUsernameFromToken(token);
        final Post post = postRepository.findByIdAndIsDeletedFalse(id);
        validation(post, username, id);
        mapToEntity(postDTO, post);
        postRepository.save(post);
    }

    private void validation(Post post, String username, Long id) {
        if (post == null) {
            throw new NotFoundException("No post found with postId: " + id);
        }
        if (!post.getUser().getUsername().equals(username)) {
            throw new NotFoundException("You can't update this post");
        }
    }

    public void delete(final Long id, String token) {
        final Post post = postRepository.findByIdAndIsDeletedFalse(id);
        String username = getUsernameFromToken(token);
        validation(post, username, id);
        post.setDeleted(true);
        postRepository.save(post);
    }

    private PostDTO mapToDTO(
            final Post post,
            final PostDTO postDTO,
            final Long currentUserId) {

        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setUserId(post.getUser().getId());
        postDTO.setLikeCount(post.getLikeCount());

        boolean liked = currentUserId != null &&
                likeRepository.getByPostIdAndUserId(post.getId(), currentUserId)
                        .map(like -> !like.isDeleted())
                        .orElse(false);

        postDTO.setLikedByCurrentUser(liked);

        return postDTO;
    }

    private void mapToEntity(final PostDTO postDTO, final Post post) {
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Post post = postRepository.findById(id).orElseThrow(NotFoundException::new);
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
        Long currentUserId = getCurrentUserId();

        return posts.stream()
                .map(post -> mapToDTO(post, new PostDTO(), currentUserId))
                .toList();
    }

    private void checkIfUserExists(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId);
        if (user == null) {
            throw new NotFoundException("No user found with userId: " + userId);
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();

        return userRepository.findByUsernameAndIsDeletedFalse(username)
                .map(User::getId)
                .orElse(null);
    }
}
