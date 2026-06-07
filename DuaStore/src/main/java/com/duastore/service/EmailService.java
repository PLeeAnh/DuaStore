package com.duastore.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(body, true);
                mailSender.send(message);
            } catch (MessagingException e) {
                System.err.println("[EmailService] Failed to send email: " + e.getMessage());
            }
        } else {
            System.out.println("[EmailService] To: " + to + " | Subject: " + subject);
        }
    }
}
