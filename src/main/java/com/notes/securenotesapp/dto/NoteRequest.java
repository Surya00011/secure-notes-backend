package com.notes.securenotesapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteRequest {

    @Schema(
            description = "Title of the note",
            example = "Meeting Notes"
    )
    private String title;

    @Schema(
            description = "Content of the note",
            example = "Discuss project updates and next steps."
    )
    private String note;

    @Schema(
            description = "Deadline for the note, if any",
            example = "2025-05-15"
    )
    private LocalDate deadline;
}
