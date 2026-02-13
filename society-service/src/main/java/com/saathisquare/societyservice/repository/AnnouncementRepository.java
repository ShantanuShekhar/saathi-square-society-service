package com.saathisquare.societyservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saathisquare.societyservice.enums.AnnouncementPriority;
import com.saathisquare.societyservice.enums.AnnouncementStatus;
import com.saathisquare.societyservice.model.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    /**
     * Find active announcements for a society (including pinned ones first)
     * Pinned announcements sorted by priority and created date, then regular announcements
     */
    @Query("SELECT a FROM Announcement a WHERE a.society.societyId = :societyId " +
           "AND a.status = 'ACTIVE' " +
           "AND a.isDeleted = false " +
           "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "ORDER BY a.isPinned DESC, a.priority DESC, a.createdAt DESC")
    Page<Announcement> findActiveAnnouncementsForSociety(
            @Param("societyId") UUID societyId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * Find all announcements for a society with filters (admin view)
     */
    @Query("SELECT a FROM Announcement a WHERE a.society.societyId = :societyId " +
           "AND a.isDeleted = false " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:priority IS NULL OR a.priority = :priority) " +
           "AND (:searchTerm IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.isPinned DESC, a.createdAt DESC")
    Page<Announcement> findBySocietyIdWithFilters(
            @Param("societyId") UUID societyId,
            @Param("status") AnnouncementStatus status,
            @Param("priority") AnnouncementPriority priority,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Find scheduled announcements that should be published now
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 'SCHEDULED' " +
           "AND a.scheduledAt <= :now " +
           "AND a.isDeleted = false")
    List<Announcement> findScheduledAnnouncementsReadyToPublish(@Param("now") LocalDateTime now);

    /**
     * Find active announcements that should be expired
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 'ACTIVE' " +
           "AND a.expiresAt IS NOT NULL " +
           "AND a.expiresAt <= :now " +
           "AND a.isDeleted = false")
    List<Announcement> findAnnouncementsReadyToExpire(@Param("now") LocalDateTime now);

    /**
     * Find pinned announcements that should be unpinned
     */
    @Query("SELECT a FROM Announcement a WHERE a.isPinned = true " +
           "AND a.pinnedUntil IS NOT NULL " +
           "AND a.pinnedUntil <= :now " +
           "AND a.isDeleted = false")
    List<Announcement> findPinnedAnnouncementsReadyToUnpin(@Param("now") LocalDateTime now);

    /**
     * Count active announcements for a society
     */
    long countBySocietySocietyIdAndStatusAndIsDeletedFalse(UUID societyId, AnnouncementStatus status);

    /**
     * Find pinned announcements for a society
     */
    @Query("SELECT a FROM Announcement a WHERE a.society.societyId = :societyId " +
           "AND a.isPinned = true " +
           "AND a.status = 'ACTIVE' " +
           "AND a.isDeleted = false " +
           "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findPinnedAnnouncementsForSociety(
            @Param("societyId") UUID societyId,
            @Param("now") LocalDateTime now);
}

