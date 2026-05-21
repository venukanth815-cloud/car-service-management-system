package com.carservice.controller;

import com.carservice.entity.Booking;
import com.carservice.entity.Customer;
import com.carservice.repository.BookingRepository;
import com.carservice.repository.CustomerRepository;
import com.carservice.repository.PaymentRepository;
import com.carservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private EmailService emailService;

    // ==============================
    // ALL BOOKINGS WITH PAGINATION
    // ==============================
    @GetMapping("/bookings")
    public String allBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Booking> bookingPage;

        if (!status.isEmpty()) {
            bookingPage = bookingRepo.findByStatusContainingIgnoreCase(status, pageable);
        } else if (!search.isEmpty()) {
            bookingPage = bookingRepo.findByCustomerNameContainingOrCustomerEmailContaining(
                search, search, pageable);
        } else {
            bookingPage = bookingRepo.findAll(pageable);
        }

        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("totalItems", bookingPage.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("size", size);

        return "admin/all-bookings";
    }

    // ==============================
    // ALL USERS
    // ==============================
    @GetMapping("/users")
    public String allUsers(Model model) {
        List<Customer> users = customerRepo.findAll();
        model.addAttribute("users", users);
        return "admin/all-users";
    }

    // ==============================
    // DELETE USER
    // ==============================
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id, Authentication auth) {
        // Prevent admin from deleting themselves
        customerRepo.findById(id).ifPresent(user -> {
            if (!user.getEmail().equals(auth.getName())) {
                customerRepo.delete(user);
            }
        });
        return "redirect:/admin/users";
    }

    // ==============================
    // APPROVE BOOKING
    // ==============================
    @GetMapping("/booking/approve/{id}")
    public String approveBooking(@PathVariable Long id) {
        bookingRepo.findById(id).ifPresent(booking -> {
            if ("PENDING".equals(booking.getStatus())) {
                booking.setStatus("APPROVED");
                bookingRepo.save(booking);
                try {
                    customerRepo.findByEmail(booking.getCustomerEmail()).ifPresent(customer ->
                        emailService.sendBookingApproved(
                            booking.getCustomerEmail(),
                            customer.getName(),
                            booking.getServiceType()
                        )
                    );
                } catch (Exception e) {
                    System.out.println("Email failed: " + e.getMessage());
                }
            }
        });
        return "redirect:/admin/bookings";
    }

    // ==============================
    // DELETE BOOKING (ADMIN)
    // ==============================
    @GetMapping("/booking/delete/{id}")
    public String deleteBooking(@PathVariable Long id) {
        bookingRepo.findById(id).ifPresent(bookingRepo::delete);
        return "redirect:/admin/bookings";
    }
}