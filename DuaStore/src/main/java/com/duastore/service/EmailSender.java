package com.duastore.service;

public interface EmailSender {
    void send(String to, String subject, String htmlContent);
    boolean isEnabled();
}
