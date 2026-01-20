package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.StripeTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StripeTransferRepository extends JpaRepository<StripeTransfer, UUID> {
    @Query("select s from StripeTransfer s where s.purchaseId = ?1")
    Optional<StripeTransfer> findBy_purchseId(UUID purchaseId);
}
