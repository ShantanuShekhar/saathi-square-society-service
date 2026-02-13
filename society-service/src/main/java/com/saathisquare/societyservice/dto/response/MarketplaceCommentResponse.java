package com.saathisquare.societyservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for marketplace post comment
 */
public record MarketplaceCommentResponse(
        UUID commentId,
        String content,
        UUID createdBy,
        String createdByName, // Will be populated from RBAC service
        UUID postId,
        LocalDateTime createdAt
) {
}

