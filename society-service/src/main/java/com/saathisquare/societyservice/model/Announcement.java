package com.saathisquare.societyservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.saathisquare.societyservice.enums.AnnouncementPriority;
import com.saathisquare.societyservice.enums.AnnouncementStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Announcement entity - for society-wide announcements with scheduling and pinning support
 */
@Entity
@Table(name = "announcement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "announcement_id", nullable = false, updatable = false, columnDefinition = "VARCHAR(36)")
    private UUID announcementId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "summary", length = 500)
    private String summary; // Short summary for list views

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "pinned_until")
    private LocalDateTime pinnedUntil; // Auto-unpin after this date

    // Scheduling fields
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt; // When to automatically publish

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // Actual publication time

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // When announcement should expire

    // Target audience (optional filtering)
    @Column(name = "target_audience", length = 100)
    private String targetAudience; // e.g., "ALL", "OWNERS", "TENANTS", "ADMINS"

    // Attachments/links
    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl; // Optional file/document link

    @Column(name = "external_link", columnDefinition = "TEXT")
    private String externalLink; // Optional external URL

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "created_by", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID createdBy; // User ID from RBAC service

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false; // Soft delete flag

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L; // Track views (can be incremented)
}

