package com.saathisquare.societyservice.service;

import com.saathisquare.societyservice.dto.request.CreateCommentRequest;
import com.saathisquare.societyservice.dto.request.CreateMarketplacePostRequest;
import com.saathisquare.societyservice.dto.request.MarketplacePostFilterRequest;
import com.saathisquare.societyservice.dto.request.UpdateMarketplacePostRequest;
import com.saathisquare.societyservice.dto.response.MarketplaceCommentResponse;
import com.saathisquare.societyservice.dto.response.MarketplacePostResponse;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.util.Response;

import java.util.UUID;

/**
 * Service interface for marketplace operations
 */
public interface MarketplaceService {

    /**
     * Create a new marketplace post
     */
    Response<MarketplacePostResponse> createPost(CreateMarketplacePostRequest request, UUID userId);

    /**
     * Update an existing marketplace post
     */
    Response<MarketplacePostResponse> updatePost(UUID postId, UpdateMarketplacePostRequest request, UUID userId);

    /**
     * Get marketplace post by ID
     */
    Response<MarketplacePostResponse> getPostById(UUID postId);

    /**
     * Get filtered and paginated marketplace posts
     */
    Response<PaginatedResponse<MarketplacePostResponse>> getFilteredPosts(MarketplacePostFilterRequest request);

    /**
     * Mark post as sold
     */
    Response<MarketplacePostResponse> markAsSold(UUID postId, UUID soldToUserId, UUID userId);

    /**
     * Delete a post (soft delete)
     */
    Response<String> deletePost(UUID postId, UUID userId);

    /**
     * Add a comment to a post
     */
    Response<MarketplaceCommentResponse> addComment(CreateCommentRequest request, UUID userId);

    /**
     * Delete a comment (soft delete)
     */
    Response<String> deleteComment(UUID commentId, UUID userId);

    /**
     * Get all comments for a post
     */
    Response<PaginatedResponse<MarketplaceCommentResponse>> getPostComments(UUID postId, int pageNo, int pageSize);
}

