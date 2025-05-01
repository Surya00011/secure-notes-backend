package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailService.class);

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("In CustomUserDetailService");
        logger.info("Attempting to load user by email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            logger.error("Email is empty");
            throw new UsernameNotFoundException("Email is empty");
        }

        Optional<User> retrievedUser = userRepository.findByEmail(email);

        User user = retrievedUser.orElseThrow(() -> {
            logger.error("User not found for email: {}", email);
            return new UsernameNotFoundException("User not found for email: " + email);
        });

        logger.info("User found: {} with email: {}", user.getUsername(), user.getEmail());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
