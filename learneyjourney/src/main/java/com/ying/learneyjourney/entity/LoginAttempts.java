package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "login_attempts")
public class LoginAttempts extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column (name = "ip_address", nullable = true)
    private String ipAddress;

    @Column (name = "user_agent", nullable = true, length = 1000)
    private String userAgent;

}