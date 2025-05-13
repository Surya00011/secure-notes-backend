package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

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

        User user = existingUser.orElse(null);

        if (user == null) {
            logger.error("User not found in DB after OAuth2 authentication.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User creation failed.");
            return;
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getUsername(), Collections.emptyList());

        logger.info("Generated UserDetails for email: {}", userDetails.getUsername());

        String token = jwtTokenProvider.generateToken(userDetails);
        logger.info("Generated JWT Token");

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        logger.info("Encoded JWT Token");

        String redirectUrl = frontendUrl + "/dashboard?token=" + encodedToken;
        logger.info("Redirecting to URL: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    private static String getEmail(Authentication authentication) {
        Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomOAuth2User customUser) {
            String email = customUser.getEmail();
            logger.info("Extracted email from CustomOAuth2User: {}", email);
            return email;
        }

        logger.error("Authentication principal is not an instance of CustomOAuth2User.");

        // Fallback logic for non-CustomOAuth2User
        if (principal instanceof OAuth2User oauthUser) {
            String email = oauthUser.getAttribute("email");
            String login = oauthUser.getAttribute("login");  // GitHub username
            String name = oauthUser.getAttribute("name");

            logger.info("OAuth2User attributes - Email: {}, Login: {}, Name: {}", email, login, name);

            if ((email == null || email.isEmpty()) && login != null && !login.isEmpty()) {
                email = login + "@githubuser.com";
                logger.warn("Email not provided by provider.{}", email);
            }

            if ((email == null || email.isEmpty()) && name != null && !name.isEmpty()) {
                String safeName = name.replaceAll("\\s+", "").toLowerCase();
                email = safeName + "@Oauthuser.com";
                logger.warn("Fallback for email using name: {}", email);
            }

            if (email == null || email.isEmpty()) {
                String randomId = UUID.randomUUID().toString().substring(0, 8);
                email = "user" + randomId + "@noemail.com";
                logger.warn("No usable data found. Fallback to random email: {}", email);
            }

            return email;
        }

        logger.error("Unknown principal type: {}", principal.getClass().getName());
        return null;
    }
}
