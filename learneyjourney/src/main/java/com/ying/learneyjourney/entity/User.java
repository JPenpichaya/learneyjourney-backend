package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumUserRoles;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "strip_connect", length = 1000)
    private String stripConnect;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private EnumUserRoles role;

}
