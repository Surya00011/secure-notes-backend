package com.notes.securenotesapp.service;

import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.AccountDeletedEvent;
import com.notes.securenotesapp.exception.UserNotFoundException;
import com.notes.securenotesapp.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;

    }

    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return user.get();
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        Long userID = user.get().getUserid();
        User deletedUser = user.get();
        userRepository.deleteById(userID);
        eventPublisher.publishEvent(new AccountDeletedEvent(deletedUser.getEmail(), deletedUser.getUsername()));
    }
}
