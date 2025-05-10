package com.notes.securenotesapp.controller;

import com.notes.securenotesapp.dto.UserProfileInfo;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileInfo> getMyProfileInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User retrivedUser = userService.findUserByEmail(email);
        System.out.println("called getMyProfileInfo");
        UserProfileInfo userProfileInfo = new UserProfileInfo();

        userProfileInfo.setUsername(retrivedUser.getUsername());
        userProfileInfo.setEmail(retrivedUser.getEmail());
        userProfileInfo.setAuthProvider(retrivedUser.getAuthProvider());

        return ResponseEntity.ok(userProfileInfo);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String,String>> deleteAccount() {
        Map<String, String> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        userService.deleteUserByEmail(email);
        response.put("message", "Account deleted");
        return ResponseEntity.ok(response);
    }
}
