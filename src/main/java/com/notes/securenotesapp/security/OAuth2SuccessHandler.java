package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider,
                                UserRepository userRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("username");
        if (username == null || username.isEmpty()) {
            username = oauthUser.getAttribute("name"); // fallback to 'name' attribute
        }
        if (username == null || username.isEmpty()) {
            assert email != null;
            username = email.substring(0, email.indexOf('@')); // fallback to email prefix
        }

        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        if (email == null || email.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found in OAuth2 response");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        if(existingUser.isPresent()) {
             user = existingUser.get();
        }else{
            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));
            userRepository.save(user);
            eventPublisher.publishEvent(new UserRegisteredEvent(email, username));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(),user.getUsername(), Collections.emptyList());
        String token = jwtTokenProvider.generateToken(userDetails);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String redirectUrl = "http://localhost:5173/dashboard?token=" + encodedToken;
        response.sendRedirect(redirectUrl);

    }
}
