package com.saathisquare.societyservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saathisquare.societyservice.model.MarketplaceImage;

@Repository
public interface MarketplaceImageRepository extends JpaRepository<MarketplaceImage, UUID> {

    /**
     * Find all images for a post, ordered by display order
     */
    List<MarketplaceImage> findByPostPostIdOrderByDisplayOrderAsc(UUID postId);

    /**
     * Find all images for multiple posts (batch loading to prevent N+1 queries)
     */
    List<MarketplaceImage> findByPostPostIdIn(List<UUID> postIds);

    /**
     * Delete all images for a specific post
     * @param postId The ID of the post whose images should be deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM MarketplaceImage mi WHERE mi.post.postId = :postId")
    void deleteByPostPostId(@Param("postId") UUID postId);
}