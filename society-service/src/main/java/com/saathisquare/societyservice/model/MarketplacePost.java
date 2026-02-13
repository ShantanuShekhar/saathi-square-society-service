package com.saathisquare.societyservice.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.saathisquare.societyservice.enums.PostStatus;
import com.saathisquare.societyservice.enums.PostType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Marketplace post entity - for buy/sell, requests, and group buys within a society
 */
@Entity
@Table(name = "marketplace_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplacePost {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "post_id", nullable = false, updatable = false, columnDefinition = "VARCHAR(36)")
    private UUID postId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    @Column(name = "price")
    private Double price; // For SELL and GROUP_BUY posts

    @Column(name = "quantity")
    private Integer quantity; // For GROUP_BUY posts

    @Column(name = "location", length = 100)
    private String location; // Where item is located/available

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "created_by", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID createdBy; // User ID from RBAC service

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MarketplaceImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MarketplaceComment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "sold_to", columnDefinition = "VARCHAR(36)")
    private UUID soldTo; // User ID who purchased/acquired the item
}

