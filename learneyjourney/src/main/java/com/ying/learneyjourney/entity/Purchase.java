package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "purchase")
public class Purchase extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private String userId;
    @Column(name = "course_id")// or Long userId if you have a user table
    private String courseId;            // idem
    @Column (name = "amount")
    private Long amount;
    @Column (name = "currency")// in smallest currency unit (e.g. satang)
    private String currency;

    @Column(unique = true, name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "status")
    private String status;              // e.g. "PAID", "REFUNDED", etc.

    @Column(name = "purchased_at")
    private OffsetDateTime purchasedAt;
    @Column(name = "stripe_event_id")
    private String stripeEventId;
}