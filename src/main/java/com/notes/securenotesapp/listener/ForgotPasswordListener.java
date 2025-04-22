package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.ForgotPasswordEvent;
import com.notes.securenotesapp.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ForgotPasswordListener {

    private final MailService mailService;

    public ForgotPasswordListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handleForgotPasswordEvent(ForgotPasswordEvent event) {
        mailService.sendPasswordResetEmail(event.getEmail(), event.getUsername(), event.getResetLink());
    }
}
