package com.saathisquare.societyservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saathisquare.societyservice.model.MarketplaceComment;

@Repository
public interface MarketplaceCommentRepository extends JpaRepository<MarketplaceComment, UUID> {

    /**
     * Find all comments for a post, excluding deleted ones
     */
    List<MarketplaceComment> findByPostPostIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID postId);

    /**
     * Count comments for a post
     */
    long countByPostPostIdAndIsDeletedFalse(UUID postId);
}

