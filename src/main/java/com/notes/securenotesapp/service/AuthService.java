package com.notes.securenotesapp.service;

import com.notes.securenotesapp.dto.RegisterRequest;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.repository.UserRepository;
import com.notes.securenotesapp.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       OtpService otpService,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.otpService = otpService;
        this.jwtTokenProvider = jwtTokenProvider;
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
            return -2; // Email not verified via OTP
        }

        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setAuthProvider(AuthProvider.LOCAL); // 👈 Set provider to LOCAL

            userRepository.save(user);

            otpService.removeVerifiedEmail(registerRequest.getEmail());
            mailService.sendRegistrationSuccessEmail(registerRequest.getEmail(), registerRequest.getUsername());

            return 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public void sendResetPasswordToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }

        String token = jwtTokenProvider.generateResetToken(email);
        String resetLink = "http://localhost:5173/reset-password?token=" + token; // Adjust frontend URL

        User user = userOptional.get();
        mailService.sendPasswordResetEmail(email, user.getUsername(), resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!jwtTokenProvider.validateResetToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token.");
        }

        String email = jwtTokenProvider.extractEmailFromResetToken(token);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        mailService.sendPasswordResetSuccessEmail(email, user.getUsername());
    }

}
