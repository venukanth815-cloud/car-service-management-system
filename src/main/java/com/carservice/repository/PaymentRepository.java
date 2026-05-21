package com.carservice.repository;

import com.carservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Total revenue for admin dashboard
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    Double getTotalRevenue();

    // All paid payments
    List<Payment> findByStatus(String status);

    // Find payment by booking id - prevents duplicate payments
    Optional<Payment> findByBookingId(Long bookingId);
}