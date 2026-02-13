package com.saathisquare.societyservice.dto.request;

import com.saathisquare.societyservice.enums.AnnouncementPriority;
import com.saathisquare.societyservice.enums.AnnouncementStatus;

import java.util.UUID;

/**
 * Request DTO for filtering announcements
 */
public record AnnouncementFilterRequest(
        UUID societyId,
        AnnouncementStatus status,
        AnnouncementPriority priority,
        String searchTerm,
        Boolean includePinnedOnly, // Filter only pinned announcements
        int pageNo,
        int pageSize
) {
    public AnnouncementFilterRequest {
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100; // Max page size
    }
}

