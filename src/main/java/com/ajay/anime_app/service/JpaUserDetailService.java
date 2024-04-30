package com.ajay.anime_app.service;

import com.ajay.anime_app.domain.SecurityUser;
import com.ajay.anime_app.domain.User;
import com.ajay.anime_app.repos.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JpaUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    public JpaUserDetailService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Could not find user");
        }
        return new SecurityUser(user.get());
    }
}
