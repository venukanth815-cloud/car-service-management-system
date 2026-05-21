package com.carservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ===== PASSWORD RESET EMAIL =====
    public void sendResetEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("AutoElite - Password Reset Request");
        message.setText(
            "Hello,\n\n" +
            "We received a request to reset your AutoElite account password.\n\n" +
            "Click the link below to reset your password:\n" +
            link + "\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
        mailSender.send(message);
    }

    // ===== BOOKING CONFIRMATION EMAIL =====
    public void sendBookingConfirmation(String to, String name, String serviceType, String date) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("AutoElite - Booking Confirmed!");
        message.setText(
            "Hello " + name + ",\n\n" +
            "Your booking has been successfully placed!\n\n" +
            "Service: " + serviceType + "\n" +
            "Date: " + date + "\n" +
            "Status: PENDING (Awaiting Approval)\n\n" +
            "We will notify you once your booking is approved.\n\n" +
            "Thank you for choosing AutoElite!\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
        mailSender.send(message);
    }

    // ===== BOOKING APPROVED EMAIL =====
    public void sendBookingApproved(String to, String name, String serviceType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("AutoElite - Booking Approved!");
        message.setText(
            "Hello " + name + ",\n\n" +
            "Great news! Your " + serviceType + " service booking has been APPROVED.\n\n" +
            "You can now proceed to make the payment from your dashboard.\n\n" +
            "Thank you for choosing AutoElite!\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
        mailSender.send(message);
    }

    // ===== PAYMENT CONFIRMATION EMAIL =====
    public void sendPaymentConfirmation(String to, String name, String serviceType, double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("AutoElite - Payment Successful!");
        message.setText(
            "Hello " + name + ",\n\n" +
            "Your payment has been received successfully!\n\n" +
            "Service: " + serviceType + "\n" +
            "Amount Paid: Rs. " + amount + "\n" +
            "Status: PAID\n\n" +
            "Thank you for choosing AutoElite!\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
        mailSender.send(message);
    }
}