package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrdersRepository extends JpaRepository<Orders, UUID> {
    @Query("select o from Orders o where o.id = ?1")
    Optional<Orders> findby_id(UUID id);
}
