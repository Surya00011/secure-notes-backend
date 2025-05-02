package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordResponse {

    @Schema(
            description = "Message indicating the result of the password reset request",
            example = "Password reset successful",
            required = true
    )
    private String message;
}
