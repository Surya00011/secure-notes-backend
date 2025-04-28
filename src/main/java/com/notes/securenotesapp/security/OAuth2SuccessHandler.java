package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

        String email = getEmail(authentication);


        if (email == null || email.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found in OAuth2 response");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = null;
        if(existingUser.isPresent()) {
             user = existingUser.get();
        }

        assert user != null;
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(),user.getUsername(), Collections.emptyList());
        String token = jwtTokenProvider.generateToken(userDetails);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String redirectUrl = "http://localhost:5173/dashboard?token=" + encodedToken;
        response.sendRedirect(redirectUrl);

    }

    private static String getEmail(Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("username");
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
