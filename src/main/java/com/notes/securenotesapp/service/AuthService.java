package com.notes.securenotesapp.service;

import com.notes.securenotesapp.dto.PreRegisterRequest;
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
    public int registerUser(PreRegisterRequest preRegisterRequest) {
        Optional<User> existingUser = userRepository.findByEmail(preRegisterRequest.getEmail());
        if (existingUser.isPresent()) {
            return 0; // Email already registered
        }

        if (!otpService.isEmailVerified(preRegisterRequest.getEmail())) {
            return -2; // Email not verified via OTP
        }

        try {
            User user = new User();
            user.setUsername(preRegisterRequest.getUsername());
            user.setPassword(passwordEncoder.encode(preRegisterRequest.getPassword()));
            user.setEmail(preRegisterRequest.getEmail());

            userRepository.save(user);

            // Clean up verified email so it can't be reused
            otpService.removeVerifiedEmail(preRegisterRequest.getEmail());

            // Send confirmation mail
            mailService.sendRegistrationSuccessEmail(preRegisterRequest.getEmail(), preRegisterRequest.getUsername());

            return 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }
}
