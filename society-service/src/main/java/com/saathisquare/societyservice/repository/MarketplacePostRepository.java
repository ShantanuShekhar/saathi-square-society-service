package com.saathisquare.societyservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saathisquare.societyservice.enums.PostStatus;
import com.saathisquare.societyservice.enums.PostType;
import com.saathisquare.societyservice.model.MarketplacePost;

@Repository
public interface MarketplacePostRepository extends JpaRepository<MarketplacePost, UUID> {

    /**
     * Find all posts for a society with pagination and filters
     */
    @Query("SELECT p FROM MarketplacePost p WHERE p.society.societyId = :societyId " +
           "AND p.status != 'DELETED' " +
           "AND (:postType IS NULL OR p.postType = :postType) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:searchTerm IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<MarketplacePost> findBySocietyIdWithFilters(
            @Param("societyId") UUID societyId,
            @Param("postType") PostType postType,
            @Param("status") PostStatus status,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Find all posts created by a user
     */
    Page<MarketplacePost> findByCreatedByAndStatusNotOrderByCreatedAtDesc(
            UUID createdBy, PostStatus status, Pageable pageable);

    /**
     * Count active posts by society
     */
    long countBySocietySocietyIdAndStatus(UUID societyId, PostStatus status);
}

