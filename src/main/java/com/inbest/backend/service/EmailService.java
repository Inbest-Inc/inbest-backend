package com.inbest.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;


    public void sendEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendWelcomeEmail(String to, String verificationLink) {
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);

        String htmlContent = templateEngine.process("welcome", context);
        sendEmail(to, "Verify Your Email", htmlContent);
    }

    public void sendForgotPasswordEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);

        String htmlContent = templateEngine.process("forgot-password", context);
        sendEmail(to, "Reset Your Password", htmlContent);
    }

}
