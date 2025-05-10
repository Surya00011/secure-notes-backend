package com.notes.securenotesapp.service;

import com.notes.securenotesapp.dto.RegisterRequest;
import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.ForgotPasswordEvent;
import com.notes.securenotesapp.event.ResetPasswordEvent;
import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.exception.InvalidTokenException;
import com.notes.securenotesapp.exception.UserNotFoundException;
import com.notes.securenotesapp.repository.UserRepository;
import com.notes.securenotesapp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       JwtTokenProvider jwtTokenProvider,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }


    public boolean isUserAlreadyRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public int registerUser(RegisterRequest registerRequest) {
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            return 0; // Email already registered
        }

        if (!otpService.isEmailVerified(registerRequest.getEmail())) {
            return -2; // Email isn't verified via OTP
        }

        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setAuthProvider(AuthProvider.LOCAL);

            userRepository.save(user);

            otpService.removeVerifiedEmail(registerRequest.getEmail());

            // Publish registration success event
            eventPublisher.publishEvent(new UserRegisteredEvent(registerRequest.getEmail(), registerRequest.getUsername()));

            return 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public void sendResetPasswordToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User Not Found");
        }

        User user = userOptional.get();
        String token = jwtTokenProvider.generateResetToken(email);
        String resetLink = frontendUrl+"/reset-password?token=" + token;

        ForgotPasswordEvent event = new ForgotPasswordEvent(email, user.getUsername(), resetLink);
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!jwtTokenProvider.validateResetToken(token)) {
            throw new InvalidTokenException("Invalid or expired token.");
        }

        String email = jwtTokenProvider.extractEmailFromResetToken(token);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        eventPublisher.publishEvent(new ResetPasswordEvent(user.getEmail(), user.getUsername()));
    }

}
