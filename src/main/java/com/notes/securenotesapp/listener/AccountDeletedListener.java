package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.AccountDeletedEvent;
import com.notes.securenotesapp.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AccountDeletedListener {

    private final MailService mailService;

    public AccountDeletedListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handleAccountDeletedEvent(AccountDeletedEvent event) {
        mailService.sendAccountDeletionEmail(event.getEmail(), event.getUsername());
    }
}
