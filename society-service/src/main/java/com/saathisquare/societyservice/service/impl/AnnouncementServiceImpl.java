package com.saathisquare.societyservice.service.impl;

import com.saathisquare.societyservice.service.BatchUserLookupService;
import com.saathisquare.societyservice.dto.request.AnnouncementFilterRequest;
import com.saathisquare.societyservice.dto.request.CreateAnnouncementRequest;
import com.saathisquare.societyservice.dto.request.UpdateAnnouncementRequest;
import com.saathisquare.societyservice.dto.response.AnnouncementResponse;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.dto.response.UserDetailsResponse;
import com.saathisquare.societyservice.enums.AnnouncementPriority;
import com.saathisquare.societyservice.enums.AnnouncementStatus;
import com.saathisquare.societyservice.exception.BusinessException;
import com.saathisquare.societyservice.exception.ResourceNotFoundException;
import com.saathisquare.societyservice.model.Announcement;
import com.saathisquare.societyservice.model.Society;
import com.saathisquare.societyservice.repository.AnnouncementRepository;
import com.saathisquare.societyservice.repository.SocietyRepository;
import com.saathisquare.societyservice.service.AnnouncementService;
import com.saathisquare.societyservice.util.Constants;
import com.saathisquare.societyservice.util.Response;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementServiceImpl.class);

    private final AnnouncementRepository announcementRepository;
    private final SocietyRepository societyRepository;
    private final BatchUserLookupService batchUserLookupService;

    @Override
    @Transactional
    public Response<AnnouncementResponse> createAnnouncement(CreateAnnouncementRequest request, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Creating announcement: title={}, societyId={}, userId={}", 
                   correlationId, request.title(), request.societyId(), userId);

        // Validate society exists
        Society society = societyRepository.findById(request.societyId())
                .orElseThrow(() -> new ResourceNotFoundException("Society not found with id: " + request.societyId()));

        // Determine status based on scheduledAt
        // If scheduled for future -> SCHEDULED
        // If not scheduled or scheduled time has passed -> ACTIVE (so residents can see it immediately)
        AnnouncementStatus status;
        if (request.scheduledAt() != null && request.scheduledAt().isAfter(LocalDateTime.now())) {
            status = AnnouncementStatus.SCHEDULED;
        } else {
            // Make it ACTIVE so residents can see it immediately
            status = AnnouncementStatus.ACTIVE;
            LOGGER.info("[{}] Announcement will be created with ACTIVE status for immediate visibility", correlationId);
        }

        // Build announcement
        Announcement announcement = Announcement.builder()
                .title(request.title())
                .content(request.content())
                .summary(request.summary())
                .status(status)
                .priority(request.priority())
                .isPinned(request.isPinned() != null ? request.isPinned() : false)
                .pinnedUntil(request.pinnedUntil())
                .scheduledAt(request.scheduledAt())
                .expiresAt(request.expiresAt())
                .targetAudience(request.targetAudience() != null ? request.targetAudience() : "ALL")
                .attachmentUrl(request.attachmentUrl())
                .externalLink(request.externalLink())
                .society(society)
                .createdBy(userId)
                .viewCount(0L)
                .build();

        announcement = announcementRepository.save(announcement);
        
        // If status is ACTIVE, set publishedAt to current time (since it's immediately visible)
        if (announcement.getStatus() == AnnouncementStatus.ACTIVE && announcement.getPublishedAt() == null) {
            announcement.setPublishedAt(LocalDateTime.now());
            announcement = announcementRepository.save(announcement);
        }
        
        LOGGER.info("[{}] Announcement created successfully: announcementId={}, status={}, publishedAt={}", 
                   correlationId, announcement.getAnnouncementId(), announcement.getStatus(), announcement.getPublishedAt());

        return buildAnnouncementResponse(announcement);
    }

    @Override
    @Transactional
    public Response<AnnouncementResponse> updateAnnouncement(UUID announcementId, UpdateAnnouncementRequest request, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Updating announcement: announcementId={}, userId={}", correlationId, announcementId, userId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        // Check ownership (admin can update any announcement)
        if (!announcement.getCreatedBy().equals(userId)) {
            // TODO: Check if user is admin of the society
            throw new BusinessException("UNAUTHORIZED", "You can only update announcements you created");
        }

        // Update fields
        if (request.title() != null) announcement.setTitle(request.title());
        if (request.content() != null) announcement.setContent(request.content());
        if (request.summary() != null) announcement.setSummary(request.summary());
        if (request.priority() != null) announcement.setPriority(request.priority());
        if (request.isPinned() != null) announcement.setIsPinned(request.isPinned());
        if (request.pinnedUntil() != null) announcement.setPinnedUntil(request.pinnedUntil());
        if (request.scheduledAt() != null) {
            announcement.setScheduledAt(request.scheduledAt());
            // Update status based on scheduledAt
            if (request.scheduledAt().isAfter(LocalDateTime.now())) {
                announcement.setStatus(AnnouncementStatus.SCHEDULED);
            } else if (announcement.getStatus() == AnnouncementStatus.SCHEDULED) {
                announcement.setStatus(AnnouncementStatus.DRAFT);
            }
        }
        if (request.expiresAt() != null) announcement.setExpiresAt(request.expiresAt());
        if (request.status() != null) announcement.setStatus(request.status());
        if (request.targetAudience() != null) announcement.setTargetAudience(request.targetAudience());
        if (request.attachmentUrl() != null) announcement.setAttachmentUrl(request.attachmentUrl());
        if (request.externalLink() != null) announcement.setExternalLink(request.externalLink());

        announcement = announcementRepository.save(announcement);
        LOGGER.info("[{}] Announcement updated successfully: announcementId={}", correlationId, announcementId);

        return buildAnnouncementResponse(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<AnnouncementResponse> getAnnouncementById(UUID announcementId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Fetching announcement: announcementId={}", correlationId, announcementId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        if (announcement.getIsDeleted()) {
            throw new ResourceNotFoundException("Announcement not found with id: " + announcementId);
        }

        return buildAnnouncementResponse(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PaginatedResponse<AnnouncementResponse>> getActiveAnnouncements(UUID societyId, int pageNo, int pageSize) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Fetching active announcements: societyId={}, pageNo={}, pageSize={}", 
                   correlationId, societyId, pageNo, pageSize);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        LocalDateTime now = LocalDateTime.now();

        Page<Announcement> announcements = announcementRepository.findActiveAnnouncementsForSociety(societyId, now, pageable);

        // Batch load user names to prevent N+1 Feign calls
        Set<UUID> userIds = announcements.getContent().stream()
                .map(Announcement::getCreatedBy)
                .collect(Collectors.toSet());
        Map<UUID, String> userNameMap = batchUserLookupService.getUserNames(userIds);

        List<AnnouncementResponse> announcementResponses = announcements.getContent().stream()
                .map(announcement -> buildAnnouncementResponseOptimized(announcement, userNameMap))
                .collect(Collectors.toList());

        PaginatedResponse<AnnouncementResponse> paginatedResponse = new PaginatedResponse<>(
                announcementResponses,
                announcements.getTotalElements(),
                announcements.getTotalPages(),
                announcements.getNumber() + 1,
                announcements.getSize()
        );

        Response<PaginatedResponse<AnnouncementResponse>> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(paginatedResponse);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PaginatedResponse<AnnouncementResponse>> getFilteredAnnouncements(AnnouncementFilterRequest request) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Fetching filtered announcements: societyId={}, status={}, priority={}, searchTerm={}", 
                   correlationId, request.societyId(), request.status(), request.priority(), request.searchTerm());

        Pageable pageable = PageRequest.of(request.pageNo() - 1, request.pageSize());

        Page<Announcement> announcements = announcementRepository.findBySocietyIdWithFilters(
                request.societyId(),
                request.status(),
                request.priority(),
                request.searchTerm(),
                pageable);

        // Filter by pinned if requested
        List<Announcement> filteredList = announcements.getContent();
        if (request.includePinnedOnly() != null && request.includePinnedOnly()) {
            filteredList = filteredList.stream()
                    .filter(Announcement::getIsPinned)
                    .collect(Collectors.toList());
        }

        // Batch load user names to prevent N+1 Feign calls
        Set<UUID> userIds = filteredList.stream()
                .map(Announcement::getCreatedBy)
                .collect(Collectors.toSet());
        Map<UUID, String> userNameMap = batchUserLookupService.getUserNames(userIds);

        List<AnnouncementResponse> announcementResponses = filteredList.stream()
                .map(announcement -> buildAnnouncementResponseOptimized(announcement, userNameMap))
                .collect(Collectors.toList());

        PaginatedResponse<AnnouncementResponse> paginatedResponse = new PaginatedResponse<>(
                announcementResponses,
                announcements.getTotalElements(),
                announcements.getTotalPages(),
                announcements.getNumber() + 1,
                announcements.getSize()
        );

        Response<PaginatedResponse<AnnouncementResponse>> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(paginatedResponse);

        return response;
    }

    @Override
    @Transactional
    public Response<AnnouncementResponse> publishAnnouncement(UUID announcementId, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Publishing announcement: announcementId={}, userId={}", correlationId, announcementId, userId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        if (!announcement.getCreatedBy().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only publish announcements you created");
        }

        if (announcement.getStatus() != AnnouncementStatus.DRAFT && announcement.getStatus() != AnnouncementStatus.SCHEDULED) {
            throw new BusinessException("INVALID_STATUS", "Only DRAFT or SCHEDULED announcements can be published");
        }

        announcement.setStatus(AnnouncementStatus.ACTIVE);
        announcement.setPublishedAt(LocalDateTime.now());
        announcement = announcementRepository.save(announcement);

        LOGGER.info("[{}] Announcement published successfully: announcementId={}", correlationId, announcementId);
        return buildAnnouncementResponse(announcement);
    }

    @Override
    @Transactional
    public Response<AnnouncementResponse> togglePin(UUID announcementId, Boolean isPinned, LocalDateTime pinnedUntil, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Toggling pin: announcementId={}, isPinned={}, userId={}", 
                   correlationId, announcementId, isPinned, userId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        if (!announcement.getCreatedBy().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only pin/unpin announcements you created");
        }

        announcement.setIsPinned(isPinned);
        announcement.setPinnedUntil(pinnedUntil);
        announcement = announcementRepository.save(announcement);

        LOGGER.info("[{}] Pin toggled successfully: announcementId={}, isPinned={}", 
                   correlationId, announcementId, isPinned);
        return buildAnnouncementResponse(announcement);
    }

    @Override
    @Transactional
    public Response<String> deleteAnnouncement(UUID announcementId, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Deleting announcement: announcementId={}, userId={}", correlationId, announcementId, userId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        if (!announcement.getCreatedBy().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only delete announcements you created");
        }

        announcement.setIsDeleted(true);
        announcementRepository.save(announcement);

        Response<String> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage("Announcement deleted successfully");
        response.setData(announcementId.toString());

        LOGGER.info("[{}] Announcement deleted successfully: announcementId={}", correlationId, announcementId);
        return response;
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElse(null);
        if (announcement != null && !announcement.getIsDeleted()) {
            announcement.setViewCount(announcement.getViewCount() + 1);
            announcementRepository.save(announcement);
        }
    }

    /**
     * Process scheduled announcements - run every minute
     * Publishes announcements that are scheduled and ready
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processScheduledAnnouncements() {
        String correlationId = "SCHEDULED_JOB";
        MDC.put("correlationId", correlationId);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Announcement> scheduledAnnouncements = announcementRepository.findScheduledAnnouncementsReadyToPublish(now);

            if (!scheduledAnnouncements.isEmpty()) {
                LOGGER.info("[{}] Processing {} scheduled announcements", correlationId, scheduledAnnouncements.size());

                for (Announcement announcement : scheduledAnnouncements) {
                    announcement.setStatus(AnnouncementStatus.ACTIVE);
                    announcement.setPublishedAt(now);
                    announcementRepository.save(announcement);
                    LOGGER.info("[{}] Published scheduled announcement: announcementId={}, title={}", 
                               correlationId, announcement.getAnnouncementId(), announcement.getTitle());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[{}] Error processing scheduled announcements", correlationId, e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Process expired announcements - run every 5 minutes
     * Marks active announcements as expired if they passed expiration date
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void processExpiredAnnouncements() {
        String correlationId = "EXPIRY_JOB";
        MDC.put("correlationId", correlationId);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Announcement> expiredAnnouncements = announcementRepository.findAnnouncementsReadyToExpire(now);

            if (!expiredAnnouncements.isEmpty()) {
                LOGGER.info("[{}] Processing {} expired announcements", correlationId, expiredAnnouncements.size());

                for (Announcement announcement : expiredAnnouncements) {
                    announcement.setStatus(AnnouncementStatus.EXPIRED);
                    announcementRepository.save(announcement);
                    LOGGER.info("[{}] Expired announcement: announcementId={}, title={}", 
                               correlationId, announcement.getAnnouncementId(), announcement.getTitle());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[{}] Error processing expired announcements", correlationId, e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Process pinned announcements - run every 10 minutes
     * Unpins announcements that passed their pinnedUntil date
     */
    @Scheduled(fixedRate = 600000) // Run every 10 minutes
    @Transactional
    public void processPinnedAnnouncements() {
        String correlationId = "UNPIN_JOB";
        MDC.put("correlationId", correlationId);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Announcement> pinnedAnnouncements = announcementRepository.findPinnedAnnouncementsReadyToUnpin(now);

            if (!pinnedAnnouncements.isEmpty()) {
                LOGGER.info("[{}] Processing {} pinned announcements to unpin", correlationId, pinnedAnnouncements.size());

                for (Announcement announcement : pinnedAnnouncements) {
                    announcement.setIsPinned(false);
                    announcement.setPinnedUntil(null);
                    announcementRepository.save(announcement);
                    LOGGER.info("[{}] Unpinned announcement: announcementId={}, title={}", 
                               correlationId, announcement.getAnnouncementId(), announcement.getTitle());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[{}] Error processing pinned announcements", correlationId, e);
        } finally {
            MDC.clear();
        }
    }

    // Helper methods
    private Response<AnnouncementResponse> buildAnnouncementResponse(Announcement announcement) {
        Response<AnnouncementResponse> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(buildAnnouncementResponseWithoutDetails(announcement).getData());
        return response;
    }

    private AnnouncementResponse buildAnnouncementResponseOptimized(Announcement announcement, Map<UUID, String> userNameMap) {
        String createdByName = userNameMap.getOrDefault(announcement.getCreatedBy(), "Admin");
        String societyName = announcement.getSociety().getName();

        return new AnnouncementResponse(
                announcement.getAnnouncementId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getSummary(),
                announcement.getStatus(),
                announcement.getPriority(),
                announcement.getIsPinned(),
                announcement.getPinnedUntil(),
                announcement.getScheduledAt(),
                announcement.getPublishedAt(),
                announcement.getExpiresAt(),
                announcement.getTargetAudience(),
                announcement.getAttachmentUrl(),
                announcement.getExternalLink(),
                announcement.getSociety().getSocietyId(),
                societyName,
                announcement.getCreatedBy(),
                createdByName,
                announcement.getViewCount(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }

    private Response<AnnouncementResponse> buildAnnouncementResponseWithoutDetails(Announcement announcement) {
        String createdByName = batchUserLookupService.getUserName(announcement.getCreatedBy());
        String societyName = announcement.getSociety().getName();

        AnnouncementResponse announcementResponse = new AnnouncementResponse(
                announcement.getAnnouncementId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getSummary(),
                announcement.getStatus(),
                announcement.getPriority(),
                announcement.getIsPinned(),
                announcement.getPinnedUntil(),
                announcement.getScheduledAt(),
                announcement.getPublishedAt(),
                announcement.getExpiresAt(),
                announcement.getTargetAudience(),
                announcement.getAttachmentUrl(),
                announcement.getExternalLink(),
                announcement.getSociety().getSocietyId(),
                societyName,
                announcement.getCreatedBy(),
                createdByName,
                announcement.getViewCount(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );

        Response<AnnouncementResponse> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(announcementResponse);
        return response;
    }

    private String getUserName(UUID userId) {
        return batchUserLookupService.getUserName(userId);
    }
}

