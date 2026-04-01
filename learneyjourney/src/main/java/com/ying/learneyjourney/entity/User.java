package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumUserRoles;
import com.ying.learneyjourney.constaint.PlanType;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.tags.form.TextareaTag;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "users")
public class User extends Auditable {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id; // Firebase UID

    @Column(nullable = true, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private EnumUserRoles role;
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanType planType = PlanType.FREE;

    @Column(name = "free_exports_used", nullable = false)
    private Integer freeExportsUsed = 0;

    @Column(name = "free_exports_limit", nullable = false)
    private Integer freeExportsLimit = 2;

    @Column(name = "daily_generations_used", nullable = false)
    private Integer dailyGenerationsUsed = 0;

    @Column(name = "daily_generation_reset_date")
    private LocalDate dailyGenerationResetDate;

    @Column(name = "generation_credits", nullable = false)
    private Integer generationCredits = 0;
}
