package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.DeadLineDayEvent;
import com.notes.securenotesapp.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DeadLineDayListener {
    private final MailService mailService;
    public DeadLineDayListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handleDeadLineDayEvent(DeadLineDayEvent event) {
        mailService.sendDeadlineReminderEmail(event.getEmail(),event.getUsername(), event.getNoteTitle() );
    }

}
