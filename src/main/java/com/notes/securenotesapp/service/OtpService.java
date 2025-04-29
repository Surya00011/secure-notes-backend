package com.notes.securenotesapp.service;

import com.notes.securenotesapp.event.OtpEmailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = new CopyOnWriteArraySet<>();

    private final ApplicationEventPublisher eventPublisher;

    public OtpService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStore.put(email, otp);

        // Publish OTP email event
        eventPublisher.publishEvent(new OtpEmailEvent(email, otp));
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpStore.containsKey(email)) return false;
        boolean isValid = otpStore.get(email).equals(otp);
        if (isValid) {
            clearOtp(email);
            markEmailAsVerified(email);
        }
        return isValid;
    }

    public void clearOtp(String email) {
        otpStore.remove(email);
    }

    public void markEmailAsVerified(String email) {
        verifiedEmails.add(email);
    }

    public boolean isEmailVerified(String email) {
        return verifiedEmails.contains(email);
    }

    public void removeVerifiedEmail(String email) {
        verifiedEmails.remove(email);
    }
}
