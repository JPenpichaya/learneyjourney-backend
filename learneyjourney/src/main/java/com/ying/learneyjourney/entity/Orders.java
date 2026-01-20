package com.ying.learneyjourney.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    private String userId;
    @Column(name = "amount")
    private Long amount;
    @Column(name = "currency")
    private String currency;
    @Column(name = "status")
    private String status; // e.g. "PENDING", "COMPLETED", etc

}