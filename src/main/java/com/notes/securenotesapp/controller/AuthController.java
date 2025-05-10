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

@RestController
@RequestMapping("/auth")
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

    // Step 1: Send OTP for registration
    @PostMapping("/pre-register")
    public ResponseEntity<ApiResponse> preRegister(@Valid @RequestBody PreRegisterRequest preRegisterRequest) {
        String email = preRegisterRequest.getEmail().toLowerCase();

        if (authService.isUserAlreadyRegistered(email)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("User already registered. Please log in."));
        }

        otpService.generateOtp(email);
        return ResponseEntity.ok(new ApiResponse("OTP has been sent to your email."));
    }

    // Step 2: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        String email = verifyOtpRequest.getEmail().toLowerCase();


        boolean isVerified = otpService.verifyOtp(email, verifyOtpRequest.getOtp());

        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("OTP verified successfully."));
        }

        return ResponseEntity.badRequest().body(new ApiResponse("Invalid OTP."));
    }

    // Step 3: Register new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String email = registerRequest.getEmail().toLowerCase();

        if (authService.isUserAlreadyRegistered(email)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("User already registered. Please log in."));
        }

        if (!otpService.isEmailVerified(email)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Email is not verified. Please verify OTP before registering."));
        }

        int status = authService.registerUser(registerRequest);

        if (status == 1) {
            otpService.removeVerifiedEmail(email); // optional cleanup
            return ResponseEntity.ok(new ApiResponse("User registered successfully."));
        } else if (status == 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("User already registered. Please log in."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred during registration."));
        }
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail().toLowerCase(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            return ResponseEntity.ok(new LoginResponse(token, "Login successful"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, "Invalid username or password"));
        }
    }

    // Forgot password: send token
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> resetPasswordRequest(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        ForgotPasswordResponse forgotPasswordResponse = new ForgotPasswordResponse();
        String email = forgotPasswordRequest.getEmail().toLowerCase();

        if (authService.isUserAlreadyRegistered(email)) {
            authService.sendResetPasswordToken(email);
            forgotPasswordResponse.setMessage("Reset password link has been sent to your email.");
        } else {
            forgotPasswordResponse.setMessage("Couldn't find your email.");
        }

        return ResponseEntity.ok(forgotPasswordResponse);
    }

    // Reset password using token
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        ResetPasswordResponse response = new ResetPasswordResponse();
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            response.setMessage("Password reset successfully.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setMessage("Something went wrong.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
