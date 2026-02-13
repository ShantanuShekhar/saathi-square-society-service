package com.saathisquare.societyservice.dto.request;

import com.saathisquare.societyservice.enums.PostStatus;
import com.saathisquare.societyservice.enums.PostType;

import java.util.UUID;

/**
 * Request DTO for filtering marketplace posts
 */
public record MarketplacePostFilterRequest(
        UUID societyId,
        PostType postType, // Filter by SELL, REQUEST, or GROUP_BUY
        PostStatus status, // Filter by ACTIVE, SOLD, CLOSED
        String searchTerm, // Search in title and description
        int pageNo,
        int pageSize
) {
    public MarketplacePostFilterRequest {
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100; // Max page size
    }
}

