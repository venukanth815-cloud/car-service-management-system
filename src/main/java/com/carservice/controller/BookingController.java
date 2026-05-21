package com.carservice.controller;

import com.carservice.entity.Booking;
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
public class BookingController {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private EmailService emailService;

    // ==============================
    // BOOK SERVICE PAGE
    // ==============================
    @GetMapping("/book")
    public String bookPage(@RequestParam(required = false) String service, Model model) {
        model.addAttribute("selectedService", service);
        return "book";
    }

    // ==============================
    // SAVE BOOKING
    // ==============================
    @PostMapping("/book")
    public String saveBooking(
            @RequestParam String customerName,
            @RequestParam String carNumber,
            @RequestParam String serviceType,
            @RequestParam String serviceDate,
            @RequestParam String serviceTime,
            Principal principal) {

        Booking booking = new Booking();
        booking.setCustomerName(customerName);
        booking.setCarNumber(carNumber);
        booking.setServiceType(serviceType);
        booking.setServiceDate(serviceDate);
        booking.setServiceTime(serviceTime);
        booking.setCustomerEmail(principal.getName());
        booking.setStatus("PENDING");
        booking.setAmount(getAmount(serviceType));

        bookingRepo.save(booking);

        // Send confirmation email
        try {
            emailService.sendBookingConfirmation(
                principal.getName(), customerName, serviceType, serviceDate
            );
        } catch (Exception e) {
            System.out.println("Email send failed: " + e.getMessage());
        }

        return "redirect:/my-bookings";
    }

    // ==============================
    // MY BOOKINGS PAGE
    // ==============================
    @GetMapping("/my-bookings")
    public String myBookings(Model model, Principal principal) {
        List<Booking> bookings = bookingRepo.findByCustomerEmail(principal.getName());
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    // ==============================
    // CANCEL BOOKING
    // ==============================
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, Principal principal) {
        bookingRepo.findById(id).ifPresent(booking -> {
            if (booking.getCustomerEmail().equals(principal.getName())
                    && "PENDING".equals(booking.getStatus())) {
                booking.setStatus("CANCELLED");
                bookingRepo.save(booking);
            }
        });
        return "redirect:/my-bookings";
    }

    // ==============================
    // DELETE BOOKING 
    // ==============================
    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id, Principal principal) {
        bookingRepo.findById(id).ifPresent(booking -> {
            if (booking.getCustomerEmail().equals(principal.getName())) {
                // Delete linked payment first to avoid foreign key error
                paymentRepo.findByBookingId(id).ifPresent(paymentRepo::delete);
                bookingRepo.delete(booking);
            }
        });
        return "redirect:/my-bookings";
    }

    // ==============================
    // BOOK AGAIN (copy old booking)
    // ==============================
    @GetMapping("/book-again/{id}")
    public String bookAgain(@PathVariable Long id, Principal principal) {
        bookingRepo.findById(id).ifPresent(old -> {
            if (!old.getCustomerEmail().equals(principal.getName())) return;

            Booking newBooking = new Booking();
            newBooking.setCustomerName(old.getCustomerName());
            newBooking.setCarNumber(old.getCarNumber());
            newBooking.setServiceType(old.getServiceType());
            newBooking.setServiceDate(old.getServiceDate());
            newBooking.setServiceTime(old.getServiceTime());
            newBooking.setCustomerEmail(principal.getName());
            newBooking.setStatus("PENDING");
            newBooking.setAmount(getAmount(old.getServiceType()));

            bookingRepo.save(newBooking);
        });
        return "redirect:/my-bookings";
    }

    // ==============================
    // AMOUNT CALCULATOR
    // ==============================
    public static double getAmount(String serviceType) {
        if (serviceType == null) return 0;
        return switch (serviceType.trim()) {
            case "Wash"        -> 500.0;
            case "Repair"      -> 1500.0;
            case "OilChange"   -> 1200.0;
            case "FullService" -> 3000.0;
            default            -> 0.0;
        };
    }
}