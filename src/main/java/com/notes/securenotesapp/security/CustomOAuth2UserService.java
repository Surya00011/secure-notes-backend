package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.exception.OAuthProviderConflictException;
import com.notes.securenotesapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private static final String GITHUB_EMAIL_API = "https://api.github.com/user/emails";

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

        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId(); // github or google
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("name");

        // Manually fetch GitHub email if not present
        if ((email == null || email.isEmpty()) && "github".equalsIgnoreCase(provider)) {
            String token = oAuth2UserRequest.getAccessToken().getTokenValue();
            email = fetchGithubEmail(token);
            logger.info("Fetched GitHub email manually: {}", email);
        }

        // Set fallback username if null
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
            logger.info("No existing user found. Creating a new user with email: {}", email);

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword("OAuth2_Default_Password"); 
            newUser.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));

            userRepository.save(newUser);

            logger.info("New user saved: {} with email: {}", newUser.getUsername(), newUser.getEmail());

            eventPublisher.publishEvent(new UserRegisteredEvent(email, username));

        } else {
            User existingUser = userOptional.get();

            if (!existingUser.getAuthProvider().name().equalsIgnoreCase(provider)) {
                logger.error("Account conflict: email {} is registered with a different provider: {}", email, existingUser.getAuthProvider().name());
                throw new OAuthProviderConflictException(
                        "Account already exists with email " + email + ". Please login using your "
                                + existingUser.getAuthProvider().name() + " account."
                );
            }

            logger.info("Existing user found: {} with email: {}", existingUser.getUsername(), existingUser.getEmail());
        }

        // Wrap original OAuth2User in CustomOAuth2User
        return new CustomOAuth2User(oAuth2User, email);
    }

    private String fetchGithubEmail(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    GITHUB_EMAIL_API,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> emails = response.getBody();

            if (emails != null) {
                for (Map<String, Object> emailEntry : emails) {
                    Boolean primary = (Boolean) emailEntry.get("primary");
                    Boolean verified = (Boolean) emailEntry.get("verified");
                    String email = (String) emailEntry.get("email");

                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return email;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch GitHub email: {}", e.getMessage());
        }
        return null;
    }
}
