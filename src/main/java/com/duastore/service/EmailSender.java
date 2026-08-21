package com.duastore.service;

/**
 * Lớp hỗ trợ xử lý email.
 */
public interface EmailSender {
    void send(String to, String subject, String htmlContent);
    boolean isEnabled();
}
