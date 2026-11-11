package com.carservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Render's free plan blocks SMTP. If BREVO_API_KEY is set we send over HTTPS instead.
    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL:}")
    private String brevoSenderEmail;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    private void send(String to, String subject, String text) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            return;
        }
        try {
            String body = json.writeValueAsString(Map.of(
                    "sender", Map.of("name", "AutoElite", "email", brevoSenderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", text));
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(8))
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 300) {
                throw new IllegalStateException("Email API returned " + res.statusCode());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Email API call failed: " + e.getMessage(), e);
        }
    }

    // ===== PASSWORD RESET EMAIL =====
    public void sendResetEmail(String to, String link) {
        send(to, "AutoElite - Password Reset Request",
            "Hello,\n\n" +
            "We received a request to reset your AutoElite account password.\n\n" +
            "Click the link below to reset your password:\n" +
            link + "\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
    }

    // ===== BOOKING CONFIRMATION EMAIL =====
    public void sendBookingConfirmation(String to, String name, String serviceType, String date) {
        send(to, "AutoElite - Booking Confirmed!",
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
    }

    // ===== BOOKING APPROVED EMAIL =====
    public void sendBookingApproved(String to, String name, String serviceType) {
        send(to, "AutoElite - Booking Approved!",
            "Hello " + name + ",\n\n" +
            "Great news! Your " + serviceType + " service booking has been APPROVED.\n\n" +
            "You can now proceed to make the payment from your dashboard.\n\n" +
            "Thank you for choosing AutoElite!\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
    }

    // ===== PAYMENT CONFIRMATION EMAIL =====
    public void sendPaymentConfirmation(String to, String name, String serviceType, double amount) {
        send(to, "AutoElite - Payment Successful!",
            "Hello " + name + ",\n\n" +
            "Your payment has been received successfully!\n\n" +
            "Service: " + serviceType + "\n" +
            "Amount Paid: Rs. " + amount + "\n" +
            "Status: PAID\n\n" +
            "Thank you for choosing AutoElite!\n\n" +
            "Best regards,\n" +
            "AutoElite Team"
        );
    }
}