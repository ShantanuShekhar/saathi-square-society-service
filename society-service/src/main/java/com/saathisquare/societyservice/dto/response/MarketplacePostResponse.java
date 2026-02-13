package com.saathisquare.societyservice.dto.response;

import com.saathisquare.societyservice.enums.PostStatus;
import com.saathisquare.societyservice.enums.PostType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for marketplace post
 */
public record MarketplacePostResponse(
        UUID postId,
        String title,
        String description,
        PostType postType,
        PostStatus status,
        Double price,
        Integer quantity,
        String location,
        String contactInfo,
        UUID societyId,
        String societyName,
        UUID createdBy,
        String createdByName, // Will be populated from RBAC service
        List<MarketplaceImageResponse> images,
        List<MarketplaceCommentResponse> comments,
        long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime soldAt,
        UUID soldTo
) {
}

