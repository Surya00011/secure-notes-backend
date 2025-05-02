package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpRequest {

    @Schema(
            description = "The email address of the user for OTP verification",
            example = "john.doe@example.com",
            required = true
    )
    @Email
    @NotBlank
    private String email;

    @Schema(
            description = "The OTP sent to the user's email for verification",
            example = "123456",
            required = true
    )
    @NotBlank
    private String otp;
}
