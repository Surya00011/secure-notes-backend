package com.notes.securenotesapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteRequest {
    private String title;
    private String note;
    private LocalDate deadline;
}
