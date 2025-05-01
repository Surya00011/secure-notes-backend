package com.notes.securenotesapp.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreviousDayRemainderEvent {
    private String toEmail;
    private String username;
    private String noteTitle;
}
