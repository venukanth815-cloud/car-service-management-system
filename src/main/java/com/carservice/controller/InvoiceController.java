package com.carservice.controller;

import com.carservice.entity.Booking;
import com.carservice.entity.Customer;
import com.carservice.entity.Payment;
import com.carservice.repository.BookingRepository;
import com.carservice.repository.CustomerRepository;
import com.carservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class InvoiceController {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private CustomerRepository customerRepo;

    // ==============================
    // VIEW INVOICE
    // ==============================
    @GetMapping("/invoice/{bookingId}")
    public String viewInvoice(
            @PathVariable Long bookingId,
            Model model,
            Authentication auth) {

        Booking booking = bookingRepo.findById(bookingId).orElse(null);

        // Security: booking must exist, belong to user, and be PAID
        if (booking == null
                || !booking.getCustomerEmail().equals(auth.getName())
                || !"PAID".equals(booking.getStatus())) {
            return "redirect:/payment";
        }

        Payment payment = paymentRepo.findByBookingId(bookingId).orElse(null);
        Customer customer = customerRepo.findByEmail(auth.getName()).orElse(null);

        // Generate invoice number
        String invoiceNumber = "AE-" + String.format("%06d", bookingId);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("customer", customer);
        model.addAttribute("invoiceNumber", invoiceNumber);

        return "invoice";
    }
}