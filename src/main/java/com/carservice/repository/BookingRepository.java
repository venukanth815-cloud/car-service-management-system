package com.carservice.repository;

import com.carservice.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ===== COUNT =====
    long countByStatus(String status);
    long countByServiceType(String serviceType);

    // ===== FIND BY STATUS =====
    List<Booking> findByStatus(String status);

    // ===== FIND BY EMAIL =====
    List<Booking> findByCustomerEmail(String customerEmail);
    List<Booking> findByCustomerEmailAndStatus(String customerEmail, String status);

    // ===== RECENT =====
    List<Booking> findTop5ByOrderByIdDesc();

    // ===== PAGINATION - ADMIN =====
    Page<Booking> findByStatusContainingIgnoreCase(String status, Pageable pageable);
    Page<Booking> findByCustomerNameContainingOrCustomerEmailContaining(
        String name, String email, Pageable pageable);

    // ===== PAGINATION - USER =====
    Page<Booking> findByCustomerEmail(String email, Pageable pageable);
}