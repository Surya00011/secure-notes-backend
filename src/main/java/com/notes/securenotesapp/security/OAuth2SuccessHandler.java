package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        logger.info("In OAuth2SuccessHandler");

        String email = getEmail(authentication);
        logger.info("Extracted Email: {}", email);

        if (email == null || email.isEmpty()) {
            logger.error("Email is empty. Sending bad request response.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found in OAuth2 response");
            return;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        logger.info("Searching for user with email: {}", email);

        User user = null;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("User found: {}", user.getUsername());
        } else {
            logger.error("User not found, creation process failed.");
        }

        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User creation failed.");
            return;
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getUsername(), Collections.emptyList());
        logger.info("Generated UserDetails for email: {}", userDetails.getUsername());

        String token = jwtTokenProvider.generateToken(userDetails);
        logger.info("Generated JWT Token");

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        logger.info("Encoded JWT Token");

        String redirectUrl = "http://localhost:5173/dashboard?token=" + encodedToken;
        logger.info("Redirecting to URL: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    private static String getEmail(Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("name");

        Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
        logger.info("OAuth2User attributes - Email: {}, Username: {}", email, username);

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
