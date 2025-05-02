package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordResponse {

    @Schema(
            description = "Message indicating the result of the forgot password request",
            example = "Password reset link sent to your email"
    )
    private String message;
}
