package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomOAuth2UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        logger.info("In CustomOAuth2UserService");

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("name");
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId(); // e.g., github, google

        // Fallback for missing username
        if (username == null || username.isEmpty()) {
            if (email != null && !email.isEmpty()) {
                username = email.split("@")[0];
            } else {
                username = "Unknown User";
            }
        }

        logger.info("Loaded OAuth2 User: {} with email: {}", username, email);
        logger.info("OAuth2 Provider: {}", provider);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // No user found, create new user
            logger.info("No existing user found. Creating a new user with email: {}", email);

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(" "); // Empty password for OAuth2 login
            newUser.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));

            userRepository.save(newUser);

            logger.info("New user saved: {} with email: {}", newUser.getUsername(), newUser.getEmail());
            eventPublisher.publishEvent(new UserRegisteredEvent(email, username));
        } else {
            User existingUser = userOptional.get();

            if (!existingUser.getAuthProvider().name().equalsIgnoreCase(provider)) {
                logger.error("Account conflict: email {} is registered with a different provider: {}", email, existingUser.getAuthProvider().name());
                throw new RuntimeException(
                        "Account already exists with email " + email + ". Please login using your "
                                + existingUser.getAuthProvider().name() + " account."
                );
            }

            logger.info("Existing user found: {} with email: {}", existingUser.getUsername(), existingUser.getEmail());
        }

        return oAuth2User;
    }
}
