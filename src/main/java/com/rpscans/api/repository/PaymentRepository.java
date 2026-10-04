package com.rpscans.api.repository;

import com.rpscans.api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByVisitId(Long visitId);
}
