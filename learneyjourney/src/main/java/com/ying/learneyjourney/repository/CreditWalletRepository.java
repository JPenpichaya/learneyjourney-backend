package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CreditWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditWalletRepository extends JpaRepository<CreditWallet, UUID> {
    Optional<CreditWallet> findByUserId(String userId);
}
