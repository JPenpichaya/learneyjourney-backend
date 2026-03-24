package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.BillingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillingTransactionRepository extends JpaRepository<BillingTransaction, UUID> {
    Optional<BillingTransaction> findByStripeSessionId(String stripeSessionId);
}
