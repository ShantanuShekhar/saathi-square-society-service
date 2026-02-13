package com.saathisquare.societyservice.service;

import com.saathisquare.societyservice.dto.request.AnnouncementFilterRequest;
import com.saathisquare.societyservice.dto.request.CreateAnnouncementRequest;
import com.saathisquare.societyservice.dto.request.UpdateAnnouncementRequest;
import com.saathisquare.societyservice.dto.response.AnnouncementResponse;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.util.Response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface for announcement operations
 */
public interface AnnouncementService {

    /**
     * Create a new announcement (as DRAFT or SCHEDULED based on scheduledAt)
     */
    Response<AnnouncementResponse> createAnnouncement(CreateAnnouncementRequest request, UUID userId);

    /**
     * Update an existing announcement
     */
    Response<AnnouncementResponse> updateAnnouncement(UUID announcementId, UpdateAnnouncementRequest request, UUID userId);

    /**
     * Get announcement by ID
     */
    Response<AnnouncementResponse> getAnnouncementById(UUID announcementId);

    /**
     * Get active announcements for residents (pinned first, then regular)
     */
    Response<PaginatedResponse<AnnouncementResponse>> getActiveAnnouncements(UUID societyId, int pageNo, int pageSize);

    /**
     * Get filtered announcements (admin view)
     */
    Response<PaginatedResponse<AnnouncementResponse>> getFilteredAnnouncements(AnnouncementFilterRequest request);

    /**
     * Publish an announcement (change status from DRAFT/SCHEDULED to ACTIVE)
     */
    Response<AnnouncementResponse> publishAnnouncement(UUID announcementId, UUID userId);

    /**
     * Pin/unpin an announcement
     */
    Response<AnnouncementResponse> togglePin(UUID announcementId, Boolean isPinned, LocalDateTime pinnedUntil, UUID userId);

    /**
     * Delete an announcement (soft delete)
     */
    Response<String> deleteAnnouncement(UUID announcementId, UUID userId);

    /**
     * Increment view count
     */
    void incrementViewCount(UUID announcementId);

    /**
     * Process scheduled announcements (called by scheduled job)
     */
    void processScheduledAnnouncements();

    /**
     * Process expired announcements (called by scheduled job)
     */
    void processExpiredAnnouncements();

    /**
     * Process pinned announcements ready to unpin (called by scheduled job)
     */
    void processPinnedAnnouncements();
}

