package com.ying.learneyjourney.entity;

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
@Entity
@Table(name = "worksheet_versions")
public class WorksheetVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worksheet_id", nullable = false)
    private Worksheet worksheet;

    @Column(name = "version_label", nullable = false, length = 20)
    private String versionLabel;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "html_content", nullable = false, columnDefinition = "TEXT")
    private String htmlContent;

    @Column(columnDefinition = "TEXT")
    private String resolvedHtml;

    @Column(name = "export_count", nullable = false)
    private Integer exportCount = 0;
}
