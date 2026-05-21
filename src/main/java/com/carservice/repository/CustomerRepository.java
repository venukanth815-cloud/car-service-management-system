package com.carservice.repository;

import com.carservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Used by Spring Security login
    Optional<Customer> findByEmail(String email);

    // Used by password reset
    Optional<Customer> findByResetToken(String token);
}