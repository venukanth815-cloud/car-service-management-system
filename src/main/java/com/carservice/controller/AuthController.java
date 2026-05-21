package com.carservice.controller;

import com.carservice.entity.Customer;
import com.carservice.repository.CustomerRepository;
import com.carservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private CustomerRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    // ==============================
    // LOGIN
    // ==============================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ==============================
    // REGISTER PAGE
    // ==============================
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ==============================
    // REGISTER - SAVE USER
    // ==============================
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        // Check if email already exists
        if (repo.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already registered. Please login.");
            return "register";
        }

        // Validate password length
        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }

        if (password.length() > 30) {
            model.addAttribute("error", "Password must not exceed 30 characters.");
            return "register";
        }

        Customer user = new Customer();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole("USER"); // ✅ Correct - no ROLE_ prefix here

        repo.save(user);

        return "redirect:/login?registered=true";
    }

    // ==============================
    // HOME PAGE
    // ==============================
    @GetMapping("/home")
    public String homePage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        repo.findByEmail(principal.getName()).ifPresentOrElse(
                user -> model.addAttribute("username", user.getName()),
                () -> model.addAttribute("username", "User")
        );

        return "home";
    }

    // ==============================
    // FORGOT PASSWORD PAGE
    // ==============================
    @GetMapping("/forgot")
    public String forgotPage() {
        return "forgot";
    }

    // ==============================
    // FORGOT PASSWORD - SEND EMAIL
    // ==============================
    @PostMapping("/forgot")
    public String processForgot(@RequestParam String email, Model model) {

        Optional<Customer> userOpt = repo.findByEmail(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that email address.");
            return "forgot";
        }

        // Generate secure token
        String token = UUID.randomUUID().toString();
        Customer user = userOpt.get();
        user.setResetToken(token);
        repo.save(user);

        // Send reset email
        String resetLink = "http://localhost:8081/reset?token=" + token;
        try {
            emailService.sendResetEmail(email, resetLink);
            model.addAttribute("message", "Password reset link sent to your email!");
        } catch (Exception e) {
            System.out.println("EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to send email: " + e.getMessage());
        }

        return "forgot";
    }

    // ==============================
    // RESET PASSWORD PAGE
    // ==============================
    @GetMapping("/reset")
    public String resetPage(@RequestParam String token, Model model) {
        // Validate token
        if (repo.findByResetToken(token).isEmpty()) {
            model.addAttribute("error", "Invalid or expired reset link.");
            return "forgot";
        }
        model.addAttribute("token", token);
        return "reset";
    }

    // ==============================
    // RESET PASSWORD - SAVE
    // ==============================
    @PostMapping("/reset")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String password,
            Model model) {

        Optional<Customer> userOpt = repo.findByResetToken(token);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid or expired reset link.");
            return "forgot";
        }

        Customer user = userOpt.get();
        user.setPassword(encoder.encode(password));
        user.setResetToken(null); // Clear token after use
        repo.save(user);

        return "redirect:/login?reset=true";
    }
}