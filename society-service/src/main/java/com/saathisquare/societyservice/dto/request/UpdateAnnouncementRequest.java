package com.saathisquare.societyservice.dto.request;

import com.saathisquare.societyservice.enums.AnnouncementPriority;
import com.saathisquare.societyservice.enums.AnnouncementStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an announcement
 */
public record UpdateAnnouncementRequest(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        String content,

        @Size(max = 500, message = "Summary must not exceed 500 characters")
        String summary,

        AnnouncementPriority priority,

        Boolean isPinned,

        LocalDateTime pinnedUntil,

        LocalDateTime scheduledAt,

        LocalDateTime expiresAt,

        AnnouncementStatus status, // For manual status updates

        String targetAudience,

        String attachmentUrl,

        String externalLink
) {
}

