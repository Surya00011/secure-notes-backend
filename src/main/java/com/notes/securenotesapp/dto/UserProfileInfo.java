package com.notes.securenotesapp.dto;

import com.notes.securenotesapp.entity.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileInfo {
    private String username;
    private String email;
    private AuthProvider authProvider;
}
