package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
    Optional<Purchase> findByStripeSessionId(String stripeSessionId);
}