package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.CommentDTO;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import com.ajay.anime_app.util.ReferencedWarning;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtService jwtService;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository, JwtService jwtService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.jwtService = jwtService;
    }

    public List<CommentDTO> findAll() {
        final List<Comment> comments = commentRepository.findByIsDeletedFalse(Sort.by("id"));
        return comments.stream().map(comment -> mapToDTO(comment, new CommentDTO())).toList();
    }

    public CommentDTO get(final Long id) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(id);
        if (comment == null) {
            throw new NotFoundException("No comment with commentId: " + id + "found");
        }
        return mapToDTO(comment, new CommentDTO());
    }

    public Long create(final CommentDTO commentDTO, Long parentId, String token) {
        String username = getUsernameFromToken(token);
        Optional<User> user = userRepository.findByUsername(username);

        validateCommentDTO(commentDTO);
        final Comment comment = new Comment();
        if (user.isPresent()) {
            comment.setUser(user.get());
        }
        if (parentId == null) {
            mapToEntity(commentDTO, comment);
        } else {
            return setOptionalParentComment(commentDTO, parentId, comment);
        }

        return commentRepository.save(comment).getId();

    }

    private Long setOptionalParentComment(CommentDTO commentDTO, Long parentId, Comment comment) {
        Comment optionalParentComment = commentRepository.findByIdAndIsDeletedFalse(parentId);
        if (optionalParentComment != null) {
            comment.setParentComment(optionalParentComment);
            mapToEntity(commentDTO, comment);
            return commentRepository.save(comment).getId();
        } else {
            throw new NotFoundException("Parent comment with ID " + parentId + " does not exist.");
        }
    }

    private void validateCommentDTO(CommentDTO commentDTO) {
        if (commentDTO.getContent() == null) {
            throw new NotFoundException("Content can't be null");
        }
    }

    public void update(final Long id, final CommentDTO commentDTO, String token) {
        final Comment comment = commentRepository.findByIdAndIsDeletedFalse(id);
        String username = getUsernameFromToken(token);

        validation(comment, username, id);
        mapToEntity(commentDTO, comment);
        commentRepository.save(comment);
    }

    private void validation(Comment comment, String username, Long id) {
        if (comment == null) {
            throw new NotFoundException("No comment with commentId: " + id + "found");
        }
        if (!comment.getUser().getUsername().equals(username)) {
            throw new NotFoundException("You can't modify someone else comment.");
        }
    }

    public void delete(final Long id, String token) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(id);
        String username = getUsernameFromToken(token);
        validation(comment, username, id);

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private CommentDTO mapToDTO(final Comment comment, final CommentDTO commentDTO) {
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setUser(comment.getUser() == null ? null : comment.getUser().getId());
        commentDTO.setPost(comment.getPost() == null ? null : comment.getPost().getId());
        return commentDTO;
    }

    private Comment mapToEntity(final CommentDTO commentDTO, final Comment comment) {
        comment.setContent(commentDTO.getContent());
        final Post post = commentDTO.getPost() == null ? null : postRepository.findById(commentDTO.getPost()).orElseThrow(() -> new NotFoundException("post not found"));
        comment.setPost(post);
        return comment;
    }

    public List<CommentDTO> getCommentsByUserId(Long userId) {
        checkIfEntityExists(userId);
        List<Comment> comments = commentRepository.findCommentsByUserIdAndIsDeletedFalse(userId);
        if (comments.isEmpty()) {
            throw new NotFoundException("No comments found for that user");
        }
        return comments.stream().map(comment -> mapToDTO(comment, new CommentDTO())).toList();
    }

    public List<CommentDTO> getCommentsByPostId(Long postId) {
        checkIfEntityExists(postId);
        List<Comment> comments = commentRepository.findCommentsByPostId(postId);
        if (comments.isEmpty()) {
            throw new NotFoundException("This Post has no comments yet");
        }
        return comments.stream().map(comment -> mapToDTO(comment, new CommentDTO())).toList();
    }

    public List<CommentDTO> getCommentsByParentId(Long parentId) {
        checkIfEntityExists(parentId);
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        List<Comment> comments = commentRepository.findByParentComment(parentComment);
        if (comments.isEmpty()) {
            throw new NotFoundException("This comment has no comments yet");
        }
        return comments.stream().map(comment -> mapToDTO(comment, new CommentDTO())).toList();


    }

    private void checkIfEntityExists(Long id) {
        Optional<User> byId = userRepository.findById(id);
        Optional<Post> isPost = postRepository.findById(id);
        Optional<Comment> isComment = commentRepository.findById(id);
        if (byId.isEmpty()
                && isPost.isEmpty()
                && isComment.isEmpty()
        ) {
            throw new NotFoundException("Please check id again " + id);
        }

    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
        final Post userPost = postRepository.findFirstByUser(user);
        if (userPost != null) {
            referencedWarning.setKey("user.post.user.referenced");
            referencedWarning.addParam(userPost.getId());
            return referencedWarning;
        }
        final Comment userComment = commentRepository.findFirstByUser(user);
        if (userComment != null) {
            referencedWarning.setKey("user.comment.user.referenced");
            referencedWarning.addParam(userComment.getId());
            return referencedWarning;
        }
        return null;
    }

    private String getUsernameFromToken(String token) {
        return jwtService.extractUsername(token.substring(7));
    }


}
