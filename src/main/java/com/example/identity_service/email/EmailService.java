package com.example.identity_service.email;

import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;

import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendVerificationEmail(String email, String verificationLink) {
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);

        String htmlContent = templateEngine.process("email/verify-email", context);
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Email Verification");
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FAILED_TO_SEND_EMAIL);
        }
    }

    public void sendPasswordResetEmail(String email, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);

        String htmlContent = templateEngine.process("email/reset-password", context);
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Password Reset");
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FAILED_TO_SEND_EMAIL);
        }
    }
}
