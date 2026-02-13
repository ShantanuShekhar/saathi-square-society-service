package com.saathisquare.societyservice.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Marketplace post image entity
 */
@Entity
@Table(name = "marketplace_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceImage {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "image_id", nullable = false, updatable = false, columnDefinition = "VARCHAR(36)")
    private UUID imageId;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl; // URL or path to stored image

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize; // Size in bytes

    @Column(name = "content_type", length = 100)
    private String contentType; // e.g., image/jpeg, image/png

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0; // Order for displaying multiple images

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private MarketplacePost post;
}

