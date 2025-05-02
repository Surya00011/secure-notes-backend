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
public class RegisterRequest {

    @Schema(
            description = "Username chosen by the user",
            example = "john_doe",
            required = true
    )
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(
            description = "Email address of the user",
            example = "user@example.com",
            required = true
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(
            description = "Password for the user account",
            example = "Password123!",
            required = true
    )
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
