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
public class PreRegisterRequest {

    @Schema(
            description = "Email address of the user",
            example = "user@example.com",
            required = true
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
}
