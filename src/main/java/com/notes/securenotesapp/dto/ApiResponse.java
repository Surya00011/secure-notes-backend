package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    @Schema(description = "Response message describing the outcome of the request", example = "Note saved successfully")
    private String message;
}
