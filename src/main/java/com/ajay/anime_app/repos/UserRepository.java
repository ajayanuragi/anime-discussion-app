package com.ajay.anime_app.repos;

import com.ajay.anime_app.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String userName);

    List<User> findByIsDeletedFalse(Sort id);

    User findByIdAndIsDeletedFalse(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);
}
