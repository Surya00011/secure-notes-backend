package com.notes.securenotesapp.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordEvent {
    private String email;
    private String username;
    private String resetLink;
}
