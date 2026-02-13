package com.saathisquare.societyservice.dto.response;

import java.util.UUID;

/**
 * Response DTO for marketplace post image
 */
public record MarketplaceImageResponse(
        UUID imageId,
        String imageUrl,
        String fileName,
        Long fileSize,
        String contentType,
        Integer displayOrder
) {
}

