package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.TransactionStatus;
import com.ying.learneyjourney.constaint.TransactionType;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "billing_transactions")
public class BillingTransaction extends Auditable {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "credits_added")
    private Integer creditsAdded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(name = "stripe_session_id", length = 255)
    private String stripeSessionId;

    @Column(name = "course_id")
    private String courseId;
}
