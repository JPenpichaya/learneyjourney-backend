package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    boolean existsByUserIdAndActiveTrueAndEndDateAfter(String userId, OffsetDateTime now);
    Optional<Subscription> findTopByUserIdAndActiveTrueOrderByEndDateDesc(String userId);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}