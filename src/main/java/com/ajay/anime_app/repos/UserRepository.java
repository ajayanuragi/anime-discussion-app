package com.ajay.anime_app.repos;

import com.ajay.anime_app.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserNameIgnoreCase(String userName);

    List<User> findByIsDeletedFalse(Sort id);


    User findByIdAndIsDeletedFalse(Long id);
}
