package com.notes.securenotesapp.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeadLineDayEvent {
    private String email;
    private String username;
    private String noteTitle;
}
