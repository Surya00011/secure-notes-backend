package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.ResetPasswordEvent;
import com.notes.securenotesapp.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResetPasswordListener {

    private final MailService mailService;

    @Async
    @EventListener
    public void handleResetPasswordEvent(ResetPasswordEvent event) {
        mailService.sendPasswordResetSuccessEmail(event.getEmail(), event.getUsername());
    }
}
