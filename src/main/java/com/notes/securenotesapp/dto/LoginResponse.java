package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    @Schema(
            description = "JWT token generated after successful login",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(
            description = "Message indicating the result of the login attempt",
            example = "Login successful"
    )
    private String message;
}
