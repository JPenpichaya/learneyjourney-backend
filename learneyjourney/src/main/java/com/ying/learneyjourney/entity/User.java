package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "users")
public class User extends Auditable {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id; // Firebase UID

    @Column(nullable = true, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;
}
