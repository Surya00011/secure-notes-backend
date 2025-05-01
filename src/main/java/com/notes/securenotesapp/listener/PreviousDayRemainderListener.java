package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.PreviousDayRemainderEvent;
import com.notes.securenotesapp.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PreviousDayRemainderListener {
    private final MailService mailService;
    public PreviousDayRemainderListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handlePreviousDayRemainder(PreviousDayRemainderEvent event) {
        mailService.sendPreviousDayReminder(event.getToEmail(), event.getUsername(), event.getNoteTitle());
    }
}
