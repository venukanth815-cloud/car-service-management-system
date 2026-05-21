package com.carservice.controller;

import com.carservice.entity.Booking;
import com.carservice.repository.BookingRepository;
import com.carservice.repository.CustomerRepository;
import com.carservice.repository.PaymentRepository;
import com.carservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired private BookingRepository bookingRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        String email = auth.getName();

        // ===== ADMIN =====
        if ("ROLE_ADMIN".equals(role)) {

            long total     = bookingRepo.count();
            long pending   = bookingRepo.countByStatus("PENDING");
            long approved  = bookingRepo.countByStatus("APPROVED");
            long paid      = bookingRepo.countByStatus("PAID");
            long completed = bookingRepo.countByStatus("COMPLETED");
            long cancelled = bookingRepo.countByStatus("CANCELLED");

            Double revenue = paymentRepo.getTotalRevenue();

            model.addAttribute("totalBookings",     total);
            model.addAttribute("waitingBookings",   pending);
            model.addAttribute("approvedBookings",  approved);
            model.addAttribute("paidBookings",      paid);
            model.addAttribute("completedBookings", completed);
            model.addAttribute("cancelledBookings", cancelled);
            model.addAttribute("revenue", revenue != null ? revenue : 0.0);
            model.addAttribute("totalCustomers", customerRepo.count());

            model.addAttribute("washCount",        bookingRepo.countByServiceType("Wash"));
            model.addAttribute("repairCount",      bookingRepo.countByServiceType("Repair"));
            model.addAttribute("oilCount",         bookingRepo.countByServiceType("OilChange"));
            model.addAttribute("fullServiceCount", bookingRepo.countByServiceType("FullService"));

            model.addAttribute("recentBookings", bookingRepo.findTop5ByOrderByIdDesc());
        }

        // ===== USER =====
        else {
            List<Booking> myBookings = bookingRepo.findByCustomerEmail(email);

            long myPending   = myBookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
            long myApproved  = myBookings.stream().filter(b -> "APPROVED".equals(b.getStatus())).count();
            long myPaid      = myBookings.stream().filter(b -> "PAID".equals(b.getStatus())).count();
            long myCompleted = myBookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count();

            double totalSpent = myBookings.stream()
                    .filter(b -> "PAID".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                    .mapToDouble(Booking::getAmount).sum();

            model.addAttribute("totalBookings",     myBookings.size());
            model.addAttribute("waitingBookings",   myPending);
            model.addAttribute("approvedBookings",  myApproved);
            model.addAttribute("paidBookings",      myPaid);
            model.addAttribute("completedBookings", myCompleted);
            model.addAttribute("revenue",           totalSpent);

            model.addAttribute("washCount",        myBookings.stream().filter(b -> "Wash".equals(b.getServiceType())).count());
            model.addAttribute("repairCount",      myBookings.stream().filter(b -> "Repair".equals(b.getServiceType())).count());
            model.addAttribute("oilCount",         myBookings.stream().filter(b -> "OilChange".equals(b.getServiceType())).count());
            model.addAttribute("fullServiceCount", myBookings.stream().filter(b -> "FullService".equals(b.getServiceType())).count());

            List<Booking> recent = myBookings.stream()
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(5).toList();
            model.addAttribute("recentBookings", recent);
        }

        return "dashboard";
    }

    @GetMapping("/booking/status/{id}/{status}")
    public String updateStatus(@PathVariable Long id, @PathVariable String status, Authentication auth) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        if (!"ROLE_ADMIN".equals(role)) return "redirect:/dashboard";

        bookingRepo.findById(id).ifPresent(booking -> {

            // PENDING → APPROVED
            if ("APPROVE".equalsIgnoreCase(status) && "PENDING".equals(booking.getStatus())) {
                booking.setStatus("APPROVED");
                bookingRepo.save(booking);
                try {
                    customerRepo.findByEmail(booking.getCustomerEmail()).ifPresent(c ->
                        emailService.sendBookingApproved(
                            booking.getCustomerEmail(), c.getName(), booking.getServiceType()
                        )
                    );
                } catch (Exception e) {
                    System.out.println("Email failed: " + e.getMessage());
                }
            }

            // PAID → COMPLETED (admin clicks Done after service is done)
            else if ("DONE".equalsIgnoreCase(status) && "PAID".equals(booking.getStatus())) {
                booking.setStatus("COMPLETED");
                bookingRepo.save(booking);
            }
        });

        return "redirect:/dashboard";
    }
}