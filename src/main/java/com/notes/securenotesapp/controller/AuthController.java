package com.notes.securenotesapp.controller;

import com.notes.securenotesapp.dto.PreRegisterRequest;
import com.notes.securenotesapp.dto.VerifyOtpRequest;
import com.notes.securenotesapp.service.AuthService;
import com.notes.securenotesapp.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "")
public class AuthController {

    private final OtpService otpService;
    private final AuthService authService;

    public AuthController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    @PostMapping("/pre-register")
    public ResponseEntity<String> preRegister(@Valid @RequestBody PreRegisterRequest preRegisterRequest) {
        otpService.generateOtp(preRegisterRequest.getEmail());
        return ResponseEntity.ok("OTP has been sent to your email.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        boolean isVerified = otpService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        if (isVerified) {
            return ResponseEntity.ok("OTP verified successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired OTP.");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody PreRegisterRequest preRegisterRequest) {
        if (!otpService.isEmailVerified(preRegisterRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email is not verified. Please verify OTP before registering.");
        }

        int status = authService.registerUser(preRegisterRequest);

        if (status == 1) {
            return ResponseEntity.ok("User registered successfully.");
        } else if (status == 0) {
            return ResponseEntity.badRequest().body("User already registered. Please log in.");
        } else {
            return ResponseEntity.internalServerError().body("An error occurred during registration.");
        }
    }
}
