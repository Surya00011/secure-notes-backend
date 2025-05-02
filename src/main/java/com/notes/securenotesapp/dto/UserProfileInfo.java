package com.notes.securenotesapp.dto;

import com.notes.securenotesapp.entity.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileInfo {

    @Schema(
            description = "Username of the user",
            example = "john_doe",
            required = true
    )
    private String username;

    @Schema(
            description = "Email address of the user",
            example = "john.doe@example.com",
            required = true
    )
    private String email;

    @Schema(
            description = "The authentication provider for the user (e.g., LOCAL, GITHUB)",
            example = "LOCAL",
            required = true
    )
    private AuthProvider authProvider;
}
