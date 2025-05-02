package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @Schema(
            description = "Reset token sent to the user's email to verify the password reset request",
            example = "abc123xyz456",
            required = true
    )
    @NotBlank(message = "Reset token is required")
    private String token;

    @Schema(
            description = "New password for the user account",
            example = "NewPassword123!",
            required = true
    )
    @NotBlank(message = "New password is required")
    private String newPassword;
}
