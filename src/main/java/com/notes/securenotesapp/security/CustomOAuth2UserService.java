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

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    public CustomOAuth2UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest){

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("username");
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        Optional<User> user  = userRepository.findByEmail(email);

        if(user.isEmpty()){
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(" ");
            newUser.setAuthProvider(AuthProvider.valueOf(provider));
            userRepository.save(newUser);
            eventPublisher.publishEvent(new UserRegisteredEvent(email, username));
        }
        return oAuth2User;
    }
}
