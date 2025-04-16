package com.notes.securenotesapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your OTP for SecureNotesApp");

            String htmlContent = "<html><body>" +
                    "<h2>OTP Verification</h2>" +
                    "<p>Hi there,</p>" +
                    "<p>Your OTP is: <strong>" + otp + "</strong></p>" +
                    "<p>This OTP is valid for a short time only. Please do not share it with anyone.</p>" +
                    "<br><p>Thanks,<br>SecureNotesApp Team</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    @Async
    public void sendRegistrationSuccessEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Welcome to SecureNotesApp 🎉");

            String htmlContent = "<html><body>" +
                    "<h2>Registration Successful</h2>" +
                    "<p>Hi <strong>" + username + "</strong>,</p>" +
                    "<p>Your account has been successfully created!</p>" +
                    "<p>We're excited to have you on board. You can now log in and start securing your notes.</p>" +
                    "<br><p>Cheers,<br>SecureNotesApp Team</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send registration success email", e);
        }
    }

}
