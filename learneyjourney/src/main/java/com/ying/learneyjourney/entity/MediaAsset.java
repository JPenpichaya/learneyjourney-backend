package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "media_asset")
public class MediaAsset extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Column(nullable = false)
    private String visibility; // "PUBLIC" or "PRIVATE"

    @Column(name = "asset_kind", nullable = false)
    private String assetKind; // "IMAGE"

    @Column
    private String description;

    @Column(nullable = false)
    private String bucket;

    @Column(name = "object_key", nullable = false)
    private String objectKey; // original key by convention

    // JSONB column
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> variants;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "asset_size")
    private Long assetSize;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "related_entity_type", nullable = false)
    private String relatedEntityType;

    @Column(name = "related_entity_id", nullable = false)
    private UUID relatedEntityId;

}