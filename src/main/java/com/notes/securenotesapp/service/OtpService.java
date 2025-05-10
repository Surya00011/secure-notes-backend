package com.notes.securenotesapp.service;

import com.notes.securenotesapp.event.OtpEmailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final SecureRandom secureRandom = new SecureRandom();

    // OTP store with expiration time
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = new CopyOnWriteArraySet<>();

    private final ApplicationEventPublisher eventPublisher;

    private static final long OTP_VALIDITY_DURATION_SECONDS = 5 * 60; // 5 minutes in seconds

    public OtpService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void generateOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        long expiresAt = Instant.now().getEpochSecond() + OTP_VALIDITY_DURATION_SECONDS;

        otpStore.put(email, new OtpEntry(otp, expiresAt));
        logger.debug("Generated OTP for email: {}, expires at: {}", email, Instant.ofEpochSecond(expiresAt));

        // Publish OTP email event
        eventPublisher.publishEvent(new OtpEmailEvent(email, otp));
    }

    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null || otp.trim().isEmpty()) {
            logger.debug("Invalid input - email or OTP is null/empty");
            return false;
        }

        // Ensure OTP is exactly 6 digits
        if (!otp.matches("\\d{6}")) {
            logger.debug("Invalid OTP format for email: {}", email);
            return false;
        }

        OtpEntry entry = otpStore.get(email);
        if (entry == null) {
            logger.debug("No OTP entry found for email: {}", email);
            return false;
        }

        long now = Instant.now().getEpochSecond();
        if (now > entry.expiresAt) {
            otpStore.remove(email);
            logger.debug("OTP expired for email: {}. Current time: {}, Expiry time: {}",
                    email, Instant.ofEpochSecond(now), Instant.ofEpochSecond(entry.expiresAt));
            return false;
        }

        boolean isValid = entry.otp.equals(otp);
        if (isValid) {
            otpStore.remove(email);
            markEmailAsVerified(email);
            logger.debug("OTP verified successfully for email: {}", email);
        } else {
            logger.debug("Invalid OTP provided for email: {}. Expected: {}, Received: {}",
                    email, entry.otp, otp);
        }

        return isValid;
    }

    public void markEmailAsVerified(String email) {
        if (email != null && !email.trim().isEmpty()) {
            verifiedEmails.add(email);
            logger.debug("Email marked as verified: {}", email);
        }
    }

    public boolean isEmailVerified(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return verifiedEmails.contains(email);
    }

    public void removeVerifiedEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            verifiedEmails.remove(email);
            logger.debug("Verified email removed: {}", email);
        }
    }

    // Internal class to track OTP and expiry
    private static class OtpEntry {
        String otp;
        long expiresAt;

        OtpEntry(String otp, long expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }
}