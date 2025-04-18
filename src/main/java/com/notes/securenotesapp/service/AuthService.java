package com.notes.securenotesapp.service;
import com.notes.securenotesapp.dto.RegisterRequest;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.repository.UserRepository;
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

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.otpService = otpService;
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

            userRepository.save(user);

            // Clean up verified email so it can't be reused
            otpService.removeVerifiedEmail(registerRequest.getEmail());

            // Send confirmation mail
            mailService.sendRegistrationSuccessEmail(registerRequest.getEmail(), registerRequest.getUsername());

            return 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }
}
