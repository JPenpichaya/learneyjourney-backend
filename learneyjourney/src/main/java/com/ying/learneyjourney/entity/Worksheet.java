package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "worksheets")
public class Worksheet extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(name = "active_version_label", length = 20)
    private String activeVersionLabel;

    @Column(name = "export_count", nullable = false)
    private Integer exportCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @OneToMany(mappedBy = "worksheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<WorksheetVersion> versions = new ArrayList<>();

}