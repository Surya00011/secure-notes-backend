package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        System.out.println("In oauth2SuccessHandler");
        String email = getEmail(authentication);
        System.out.println("Extracted Email: " + email);

        if (email == null || email.isEmpty()) {
            System.out.println("Email is empty. Sending bad request response.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found in OAuth2 response");
            return;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        System.out.println("Searching for user with email: " + email);

        User user = null;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            System.out.println("User found: " + user.getUsername());
        } else {
            System.out.println("User not found, creating new user.");
        }

        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User creation failed.");
            return;
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getUsername(), Collections.emptyList());
        System.out.println("Generated UserDetails: " + userDetails.getUsername());

        String token = jwtTokenProvider.generateToken(userDetails);
        System.out.println("Generated JWT Token: " + token);

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        System.out.println("Encoded JWT Token: " + encodedToken);

        String redirectUrl = "http://localhost:5173/dashboard?token=" + encodedToken;
        System.out.println("Redirect URL: " + redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    private static String getEmail(Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("name");
        System.out.println("OAuth2User email: " + email + ", username: " + username);

        if (username == null || username.isEmpty()) {
            username = oauthUser.getAttribute("name");
        }
        if (username == null || username.isEmpty()) {
            assert email != null;
            username = email.substring(0, email.indexOf('@')); // fallback to email prefix
        }
        return email;
    }
}
