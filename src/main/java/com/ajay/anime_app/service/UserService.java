package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.Comment;
import com.ajay.anime_app.domain.Post;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.model.UserDTO;
import com.ajay.anime_app.repos.CommentRepository;
import com.ajay.anime_app.repos.PostRepository;
import com.ajay.anime_app.repos.UserRepository;
import com.ajay.anime_app.util.NotFoundException;
import com.ajay.anime_app.util.ReferencedWarning;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final PostRepository postRepository, final CommentRepository commentRepository, final JwtService jwtService, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> findAll() {
        final List<User> users = userRepository.findByIsDeletedFalse((Sort.by("id")));
        return users.stream().map(user -> mapToDTO(user, new UserDTO())).toList();
    }

    public UserDTO get(final Long id) {
        final User user = userRepository.findByIdAndIsDeletedFalse(id);
        if (user == null) {
            throw new NotFoundException("User not found with userId: " + id);
        }
        return mapToDTO(user, new UserDTO());
    }

    public void update(final Long id, final UserDTO userDTO, String token) {
        final User user = userRepository.findByIdAndIsDeletedFalse(id);
        String username = getUsernameFromToken(token);
        validation(user, username, id);
        mapToEntity(userDTO, user);
        userRepository.save(user);
    }

    private String getUsernameFromToken(String token) {
        return jwtService.extractUsername(token.substring(7));
    }

    private void validation(User user, String username, long id) {
        if (user == null) {
            throw new NotFoundException("User not found with userId: " + id);
        }
        if (!user.getUsername().equals(username)) {
            throw new NotFoundException("You can't modify this user");
        }
    }

    public void delete(final Long id, String token) {
        final User user = userRepository.findByIdAndIsDeletedFalse(id);
        String username = getUsernameFromToken(token);
        validation(user, username, id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setUsername(user.getUsername());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return user;
    }

    public boolean userNameExists(final String userName) {
        return userRepository.existsByUsernameIgnoreCase(userName);
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

}
