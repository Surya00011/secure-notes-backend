package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("In CustomUserDetailService");
        System.out.println("Attempting to load user by email: " + email);

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email is empty");
        }

        Optional<User> retrivedUser = Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email)));

        User user = null;
        if (retrivedUser.isPresent()) {
            user = retrivedUser.get();
            System.out.println("User found: " + user.getUsername() + " with email: " + user.getEmail());
        } else {
            System.out.println("User not found with email: " + email);
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}
