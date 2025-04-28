package com.notes.securenotesapp.security;

import com.notes.securenotesapp.entity.AuthProvider;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomOAuth2UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {

        System.out.println("In CustomOAuth2UserService");

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        String sd = oAuth2User.getAttributes().toString();
        System.out.println(sd);
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("name");
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        System.out.println("Loaded OAuth2 User: " + username + " with email: " + email);
        System.out.println("OAuth2 Provider: " + provider);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            System.out.println("No existing user found. Creating a new user with email: " + email);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(" ");  // Empty password for OAuth2 login
            newUser.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));
            userRepository.save(newUser);

            System.out.println("New user saved: " + newUser.getUsername() + " with email: " + newUser.getEmail());
            eventPublisher.publishEvent(new UserRegisteredEvent(email, username));
        } else {
            System.out.println("Existing user found: " + user.get().getUsername() + " with email: " + user.get().getEmail());
        }

        return oAuth2User;
    }
}
