package com.notes.securenotesapp.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = new HashSet<>();

    private final MailService mailService;

    public OtpService(MailService mailService) {
        this.mailService = mailService;
    }

    //Generate OTP and store to hashmap
    public void generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        otpStore.put(email, otp);
        mailService.sendOtpEmail(email, otp);
    }

    //VerifyOTP by incomingEmail
    public boolean verifyOtp(String email, String otp) {
        if (!otpStore.containsKey(email)) return false;
        boolean isValid = otpStore.get(email).equals(otp);
        if (isValid) {
            clearOtp(email);
            markEmailAsVerified(email);
        }
        return isValid;
    }

    //Clear memory
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
