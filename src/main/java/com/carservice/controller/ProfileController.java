package com.carservice.controller;

import com.carservice.entity.Customer;
import com.carservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private PasswordEncoder encoder;

    // ==============================
    // VIEW PROFILE
    // ==============================
    @GetMapping("/profile")
    public String profilePage(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";
        Customer customer = customerRepo.findByEmail(auth.getName()).orElse(null);
        if (customer == null) return "redirect:/dashboard";
        model.addAttribute("customer", customer);
        return "profile";
    }

    // ==============================
    // UPDATE NAME
    // ==============================
    @PostMapping("/profile/update-name")
    public String updateName(@RequestParam String name, Authentication auth, RedirectAttributes ra) {
        customerRepo.findByEmail(auth.getName()).ifPresent(customer -> {
            customer.setName(name.trim());
            customerRepo.save(customer);
        });
        ra.addFlashAttribute("success", "Name updated successfully!");
        return "redirect:/profile";
    }

    // ==============================
    // UPDATE PASSWORD
    // ==============================
    @PostMapping("/profile/update-password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            RedirectAttributes ra) {

        Customer customer = customerRepo.findByEmail(auth.getName()).orElse(null);
        if (customer == null) return "redirect:/login";

        if (!encoder.matches(currentPassword, customer.getPassword())) {
            ra.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/profile";
        }

        customer.setPassword(encoder.encode(newPassword));
        customerRepo.save(customer);
        ra.addFlashAttribute("success", "Password updated successfully!");
        return "redirect:/profile";
    }
}