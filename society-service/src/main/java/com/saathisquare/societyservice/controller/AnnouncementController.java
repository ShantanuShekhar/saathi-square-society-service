package com.saathisquare.societyservice.controller;

import com.saathisquare.societyservice.client.RbacClient;
import com.saathisquare.societyservice.dto.request.AnnouncementFilterRequest;
import com.saathisquare.societyservice.dto.request.CreateAnnouncementRequest;
import com.saathisquare.societyservice.dto.request.UpdateAnnouncementRequest;
import com.saathisquare.societyservice.dto.response.AnnouncementResponse;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.dto.response.UserDetailsResponse;
import com.saathisquare.societyservice.service.AnnouncementService;
import com.saathisquare.societyservice.util.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller for announcement operations
 * Handles CRUD operations, scheduling, and pinning for society announcements
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementController.class);
    private final AnnouncementService announcementService;
    private final RbacClient rbacClient;

    /**
     * Create a new announcement
     * POST /api/announcements
     */
    @PostMapping
    public ResponseEntity<Response<AnnouncementResponse>> createAnnouncement(
            @RequestBody @Valid CreateAnnouncementRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] POST /api/announcements - userIdHeader={}", correlationId, userIdHeader);
        
        // X-User-Id might be email/username or UUID - handle both cases
        UUID userId = resolveUserId(userIdHeader, correlationId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("ERROR", "Failed to resolve user: " + userIdHeader, null));
        }
        
        Response<AnnouncementResponse> response = announcementService.createAnnouncement(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Helper method to resolve user ID from email/username or UUID
     * @param userIdHeader The X-User-Id header value (can be UUID or email/username)
     * @param correlationId Correlation ID for logging
     * @return UUID if resolved successfully, null otherwise
     */
    private UUID resolveUserId(String userIdHeader, String correlationId) {
        try {
            // Try to parse as UUID first
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            // If not a UUID, it's likely an email/username - look up user from RBAC service
            LOGGER.info("[{}] userIdHeader is not a UUID, looking up user by email/username: {}", correlationId, userIdHeader);
            try {
            	LOGGER.info("Inside try block to lookup user by email/username: {}", userIdHeader);
                ResponseEntity<Response<UserDetailsResponse>> userResponse = rbacClient.getLoginDetailsByUsername(userIdHeader);
                if (userResponse.getBody() == null || userResponse.getBody().getData() == null) {
                    LOGGER.error("[{}] User not found for email/username: {}", correlationId, userIdHeader);
                    return null;
                }
                LOGGER.info(" try block to lookup user by email/username: {}", userIdHeader);
                UUID userId = userResponse.getBody().getData().getId();
                LOGGER.info("[{}] Found user ID: {} for email/username: {}", correlationId, userId, userIdHeader);
                return userId;
            } catch (Exception ex) {
                LOGGER.error("[{}] Error looking up user by email/username: {}, error: {}", correlationId, userIdHeader, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * Update an existing announcement
     * PUT /api/announcements/{announcementId}
     */
    @PutMapping("/{announcementId}")
    public ResponseEntity<Response<AnnouncementResponse>> updateAnnouncement(
            @PathVariable UUID announcementId,
            @RequestBody @Valid UpdateAnnouncementRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] PUT /api/announcements/{} - userIdHeader={}", correlationId, announcementId, userIdHeader);
        
        UUID userId = resolveUserId(userIdHeader, correlationId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("ERROR", "Failed to resolve user: " + userIdHeader, null));
        }
        
        Response<AnnouncementResponse> response = announcementService.updateAnnouncement(announcementId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get announcement by ID
     * GET /api/announcements/{announcementId}
     */
    @GetMapping("/{announcementId}")
    public ResponseEntity<Response<AnnouncementResponse>> getAnnouncement(@PathVariable UUID announcementId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] GET /api/announcements/{}", correlationId, announcementId);
        
        // Increment view count
        announcementService.incrementViewCount(announcementId);
        
        Response<AnnouncementResponse> response = announcementService.getAnnouncementById(announcementId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active announcements for residents (pinned first)
     * GET /api/announcements/active
     */
    @GetMapping("/active")
    public ResponseEntity<Response<PaginatedResponse<AnnouncementResponse>>> getActiveAnnouncements(
            @RequestParam UUID societyId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] GET /api/announcements/active - societyId={}, pageNo={}, pageSize={}", 
                   correlationId, societyId, pageNo, pageSize);
        
        Response<PaginatedResponse<AnnouncementResponse>> response = 
                announcementService.getActiveAnnouncements(societyId, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Get filtered announcements (admin view)
     * POST /api/announcements/filter
     */
    @PostMapping("/filter")
    public ResponseEntity<Response<PaginatedResponse<AnnouncementResponse>>> getFilteredAnnouncements(
            @RequestBody AnnouncementFilterRequest request) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] POST /api/announcements/filter - societyId={}", correlationId, request.societyId());
        
        Response<PaginatedResponse<AnnouncementResponse>> response = 
                announcementService.getFilteredAnnouncements(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Publish an announcement
     * PUT /api/announcements/{announcementId}/publish
     */
    @PutMapping("/{announcementId}/publish")
    public ResponseEntity<Response<AnnouncementResponse>> publishAnnouncement(
            @PathVariable UUID announcementId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] PUT /api/announcements/{}/publish - userIdHeader={}", correlationId, announcementId, userIdHeader);
        
        UUID userId = resolveUserId(userIdHeader, correlationId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("ERROR", "Failed to resolve user: " + userIdHeader, null));
        }
        
        Response<AnnouncementResponse> response = announcementService.publishAnnouncement(announcementId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Pin/unpin an announcement
     * PUT /api/announcements/{announcementId}/pin
     */
    @PutMapping("/{announcementId}/pin")
    public ResponseEntity<Response<AnnouncementResponse>> togglePin(
            @PathVariable UUID announcementId,
            @RequestParam Boolean isPinned,
            @RequestParam(required = false) String pinnedUntil, // Accept String and parse
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] PUT /api/announcements/{}/pin - isPinned={}, userIdHeader={}", 
                   correlationId, announcementId, isPinned, userIdHeader);
        
        UUID userId = resolveUserId(userIdHeader, correlationId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("ERROR", "Failed to resolve user: " + userIdHeader, null));
        }
        
        // Parse pinnedUntil string to LocalDateTime if provided
        LocalDateTime pinnedUntilDate = null;
        if (pinnedUntil != null && !pinnedUntil.isEmpty()) {
            try {
                // Try ISO-8601 format first (e.g., "2025-01-28T00:00:00")
                pinnedUntilDate = LocalDateTime.parse(pinnedUntil);
            } catch (Exception e) {
                try {
                    // Try ISO-8601 with timezone (e.g., "2025-01-28T00:00:00Z")
                    pinnedUntilDate = java.time.ZonedDateTime.parse(pinnedUntil).toLocalDateTime();
                } catch (Exception e2) {
                    LOGGER.warn("[{}] Failed to parse pinnedUntil date: {}. Error: {}", 
                               correlationId, pinnedUntil, e2.getMessage());
                    // Continue with null, which is acceptable
                }
            }
        }
        
        Response<AnnouncementResponse> response = announcementService.togglePin(announcementId, isPinned, pinnedUntilDate, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an announcement
     * DELETE /api/announcements/{announcementId}
     */
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<Response<String>> deleteAnnouncement(
            @PathVariable UUID announcementId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] DELETE /api/announcements/{} - userIdHeader={}", correlationId, announcementId, userIdHeader);
        
        UUID userId = resolveUserId(userIdHeader, correlationId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("ERROR", "Failed to resolve user: " + userIdHeader, null));
        }
        
        Response<String> response = announcementService.deleteAnnouncement(announcementId, userId);
        return ResponseEntity.ok(response);
    }
}

