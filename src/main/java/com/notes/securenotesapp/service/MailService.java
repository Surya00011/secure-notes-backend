package com.notes.securenotesapp.service;

import com.notes.securenotesapp.exception.EmailSendException;
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
            throw new EmailSendException("Failed to send OTP email");
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
            throw new EmailSendException("Failed to send registration success email");
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String resetLink) {
        String subject = "Reset Your Password - Secure Notes App";
        String body = "<html><body>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>You requested a password reset. Click the link below to reset your password:</p>"
                + "<p><a href=\"" + resetLink + "\">Reset Password</a></p>"
                + "<p>If you didn't request this, you can ignore this email.</p>"
                + "<br><p>— Secure Notes App Team</p>"
                + "</body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true enables HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send reset password email");
        }
    }

    @Async
    public void sendPasswordResetSuccessEmail(String toEmail, String username) {
        String subject = "Password Changed Successfully";
        String body = "<html><body>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>Your password has been changed successfully.</p>"
                + "<p>If you did not perform this action, please contact support immediately.</p>"
                + "<br><p>— Secure Notes App Team</p>"
                + "</body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true enables HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send password reset success email");
        }
    }

    @Async
    public void sendAccountDeletionEmail(String toEmail, String username) {
        String subject = "Account Deleted Successfully";
        String body = "<html><body>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>Your account has been <strong>successfully deleted</strong> from Secure Notes App.</p>"
                + "<p>We're sorry to see you go. If this was a mistake, feel free to reach out to our support team.</p>"
                + "<br><p>— Secure Notes App Team</p>"
                + "</body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send account deletion email");
        }
    }

    @Async
    public void sendDeadlineReminderEmail(String toEmail, String username, String noteTitle) {
        String subject = "Note Deadline Reminder";
        String body = "<html><body>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>This is a reminder that your note titled <strong>\"" + noteTitle + "\"</strong> has a deadline today.</p>"
                + "<p>Don't forget to complete it!</p>"
                + "<br><p>— Secure Notes App Team</p>"
                + "</body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send deadline reminder email");
        }
    }

    @Async
    public void sendPreviousDayReminder(String toEmail, String username, String noteTitle) {
        String subject = "Upcoming Note Deadline - Reminder";
        String body = "<html><body>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>This is a reminder that your note titled <strong>\"" + noteTitle + "\"</strong> is due <strong>tomorrow</strong>.</p>"
                + "<p>Please make sure to review or complete it before the deadline.</p>"
                + "<br><p>— Secure Notes App Team</p>"
                + "</body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send deadline reminder email");
        }
    }

}
