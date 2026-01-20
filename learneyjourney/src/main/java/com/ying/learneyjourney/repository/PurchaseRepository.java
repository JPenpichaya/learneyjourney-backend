package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
    Optional<Purchase> findByStripeSessionId(String stripeSessionId);

    @Query("select (count(p) > 0) from Purchase p where p.stripeEventId = ?1")
    boolean exist_StripEvenId(String stripeEventId);

    @Query("select p from Purchase p where p.orderId = ?1")
    List<Purchase> findby_orderId(String orderId);
}