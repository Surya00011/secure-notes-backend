package com.notes.securenotesapp.controller;

import com.notes.securenotesapp.dto.*;
import com.notes.securenotesapp.security.JwtTokenProvider;
import com.notes.securenotesapp.service.AuthService;
import com.notes.securenotesapp.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "")
public class AuthController {

    private final OtpService otpService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthController(OtpService otpService, AuthService authService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.otpService = otpService;
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
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
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {

        if (authService.isUserAlreadyRegistered(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("User already registered. Please log in.");
        }

        if (!otpService.isEmailVerified(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email is not verified. Please verify OTP before registering.");
        }

        int status = authService.registerUser(registerRequest);

        if (status == 1) {
            return ResponseEntity.ok("User registered successfully.");
        } else if (status == 0) {
            return ResponseEntity.badRequest().body("User already registered. Please log in.");
        } else {
            return ResponseEntity.internalServerError().body("An error occurred during registration.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            LoginResponse loginResponse = new LoginResponse(token, "Login successful");
            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, "Invalid username or password"));
        }
    }


}

