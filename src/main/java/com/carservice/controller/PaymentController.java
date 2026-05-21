package com.carservice.controller;

import com.carservice.entity.Booking;
import com.carservice.entity.Payment;
import com.carservice.repository.BookingRepository;
import com.carservice.repository.CustomerRepository;
import com.carservice.repository.PaymentRepository;
import com.carservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class PaymentController {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private EmailService emailService;

    // ==============================
    // PAYMENT LIST PAGE
    // ==============================
    @GetMapping("/payment")
    public String paymentList(Model model, Principal principal) {

        String email = principal.getName();

        // Bookings approved and ready to pay
        List<Booking> pending = bookingRepo.findByCustomerEmailAndStatus(email, "APPROVED");
        model.addAttribute("pending", pending);

        // Already paid bookings - directly query by email and status
        List<Booking> paidBookings = bookingRepo.findByCustomerEmailAndStatus(email, "PAID");
        model.addAttribute("paidBookings", paidBookings);

        return "payment-list";
    }

    // ==============================
    // OPEN PAYMENT PAGE
    // ==============================
    @GetMapping("/payment/{id}")
    public String paymentPage(
            @PathVariable Long id,
            Model model,
            Principal principal) {

        Booking booking = bookingRepo.findById(id).orElse(null);

        // Security check: booking must exist, belong to user, and be APPROVED
        if (booking == null
                || !booking.getCustomerEmail().equals(principal.getName())
                || (!"APPROVED".equals(booking.getStatus()) && !"PAID".equals(booking.getStatus()))) {
            return "redirect:/payment";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("amount", booking.getAmount());

        return "payment";
    }

    // ==============================
    // CONFIRM PAYMENT
    // ==============================
    @PostMapping("/payment/{id}")
    public String confirmPayment(
            @PathVariable Long id,
            Principal principal) {

        Booking booking = bookingRepo.findById(id).orElse(null);

        // Security check
        if (booking == null
                || !booking.getCustomerEmail().equals(principal.getName())
                || !"APPROVED".equals(booking.getStatus())) {
            return "redirect:/payment";
        }

        // Prevent duplicate payments
        if (paymentRepo.findByBookingId(id).isPresent()) {
            return "redirect:/payment";
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getAmount());
        payment.setStatus("PAID");
        paymentRepo.save(payment);

        // Update booking status
        booking.setStatus("PAID");
        bookingRepo.save(booking);

        // Send payment confirmation email
        try {
            customerRepo.findByEmail(principal.getName()).ifPresent(customer -> {
                emailService.sendPaymentConfirmation(
                    principal.getName(),
                    customer.getName(),
                    booking.getServiceType(),
                    booking.getAmount()
                );
            });
        } catch (Exception e) {
            System.out.println("Payment email failed: " + e.getMessage());
        }

        return "redirect:/payment/" + id;
    }
}