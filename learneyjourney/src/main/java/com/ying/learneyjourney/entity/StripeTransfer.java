package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "stripe_transfer")
public class StripeTransfer extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "purchase_id")
    private UUID purchaseId;
    @Column(name = "tutor_profile_id")
    private UUID tutorProfileId;
    @Column(name = "stripe_account_id")
    private String stripeAccountId;
    @Column(name = "amount")
    private Long amount;
    private String currency;
    @Column(name = "stripe_transfer_id")
    private String stripeTransferId;
    @Column(name = "status")
    private String status;

}