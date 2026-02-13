package com.saathisquare.societyservice.dto.response;

import com.saathisquare.societyservice.enums.AnnouncementPriority;
import com.saathisquare.societyservice.enums.AnnouncementStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for announcement
 */
public record AnnouncementResponse(
        UUID announcementId,
        String title,
        String content,
        String summary,
        AnnouncementStatus status,
        AnnouncementPriority priority,
        Boolean isPinned,
        LocalDateTime pinnedUntil,
        LocalDateTime scheduledAt,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt,
        String targetAudience,
        String attachmentUrl,
        String externalLink,
        UUID societyId,
        String societyName,
        UUID createdBy,
        String createdByName, // Will be populated from RBAC service
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

