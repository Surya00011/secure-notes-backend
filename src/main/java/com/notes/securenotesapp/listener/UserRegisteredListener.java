package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.UserRegisteredEvent;
import com.notes.securenotesapp.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredListener {

    private final MailService mailService;

    public UserRegisteredListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        mailService.sendRegistrationSuccessEmail(event.getEmail(), event.getUsername());
    }
}
