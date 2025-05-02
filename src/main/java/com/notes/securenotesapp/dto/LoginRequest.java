package com.notes.securenotesapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    private String email;

    @NotBlank
    @Schema(description = "User's password", example = "SecurePassword123!", required = true)
    private String password;
}
