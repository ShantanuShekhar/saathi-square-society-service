package com.saathisquare.societyservice.dto.request;

import com.saathisquare.societyservice.enums.AnnouncementPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating an announcement
 */
public record CreateAnnouncementRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        @Size(max = 500, message = "Summary must not exceed 500 characters")
        String summary,

        @NotNull(message = "Society ID is required")
        UUID societyId,

        @NotNull(message = "Priority is required")
        AnnouncementPriority priority,

        Boolean isPinned, // Optional, defaults to false

        LocalDateTime pinnedUntil, // Optional, auto-unpin date

        LocalDateTime scheduledAt, // Optional, schedule for future publication

        LocalDateTime expiresAt, // Optional, expiration date

        String targetAudience, // Optional, e.g., "ALL", "OWNERS", "TENANTS"

        String attachmentUrl, // Optional file/document link

        String externalLink // Optional external URL
) {
    public CreateAnnouncementRequest {
        if (scheduledAt != null && expiresAt != null && scheduledAt.isAfter(expiresAt)) {
            throw new IllegalArgumentException("Scheduled date cannot be after expiration date");
        }
    }
}

