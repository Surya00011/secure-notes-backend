package com.notes.securenotesapp.listener;

import com.notes.securenotesapp.event.OtpEmailEvent;
import com.notes.securenotesapp.service.MailService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
public class OtpEmailListener {

    private final MailService mailService;

    @Async
    @EventListener
    public void handleOtpEmailEvent(OtpEmailEvent event) {
        mailService.sendOtpEmail(event.getEmail(), event.getOtp());
    }

}
