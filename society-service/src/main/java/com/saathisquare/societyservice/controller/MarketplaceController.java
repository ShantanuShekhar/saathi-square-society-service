package com.saathisquare.societyservice.controller;

import com.saathisquare.societyservice.dto.request.CreateCommentRequest;
import com.saathisquare.societyservice.dto.request.CreateMarketplacePostRequest;
import com.saathisquare.societyservice.dto.request.MarketplacePostFilterRequest;
import com.saathisquare.societyservice.dto.request.UpdateMarketplacePostRequest;
import com.saathisquare.societyservice.dto.response.MarketplaceCommentResponse;
import com.saathisquare.societyservice.dto.response.MarketplacePostResponse;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.service.MarketplaceService;
import com.saathisquare.societyservice.util.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for marketplace operations
 * Handles posts, comments, and filtering for society marketplace
 */
@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceController.class);
    private final MarketplaceService marketplaceService;

    /**
     * Create a new marketplace post
     * POST /api/marketplace/posts
     */
    @PostMapping("/posts")
    public ResponseEntity<Response<MarketplacePostResponse>> createPost(
            @RequestBody @Valid CreateMarketplacePostRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] POST /api/marketplace/posts - userId={}", correlationId, userIdHeader);
        
        UUID userId = UUID.fromString(userIdHeader);
        Response<MarketplacePostResponse> response = marketplaceService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing marketplace post
     * PUT /api/marketplace/posts/{postId}
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<Response<MarketplacePostResponse>> updatePost(
            @PathVariable UUID postId,
            @RequestBody @Valid UpdateMarketplacePostRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] PUT /api/marketplace/posts/{} - userId={}", correlationId, postId, userIdHeader);
        
        UUID userId = UUID.fromString(userIdHeader);
        Response<MarketplacePostResponse> response = marketplaceService.updatePost(postId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get marketplace post by ID
     * GET /api/marketplace/posts/{postId}
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Response<MarketplacePostResponse>> getPost(@PathVariable UUID postId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] GET /api/marketplace/posts/{}", correlationId, postId);
        
        Response<MarketplacePostResponse> response = marketplaceService.getPostById(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get filtered marketplace posts
     * POST /api/marketplace/posts/filter
     */
    @PostMapping("/posts/filter")
    public ResponseEntity<Response<PaginatedResponse<MarketplacePostResponse>>> getFilteredPosts(
            @RequestBody MarketplacePostFilterRequest request) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] POST /api/marketplace/posts/filter - societyId={}", correlationId, request.societyId());
        
        Response<PaginatedResponse<MarketplacePostResponse>> response = marketplaceService.getFilteredPosts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark post as sold
     * PUT /api/marketplace/posts/{postId}/mark-sold
     */
    @PutMapping("/posts/{postId}/mark-sold")
    public ResponseEntity<Response<MarketplacePostResponse>> markAsSold(
            @PathVariable UUID postId,
            @RequestParam(required = false) UUID soldTo,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] PUT /api/marketplace/posts/{}/mark-sold - userId={}", correlationId, postId, userIdHeader);
        
        UUID userId = UUID.fromString(userIdHeader);
        Response<MarketplacePostResponse> response = marketplaceService.markAsSold(postId, soldTo, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a marketplace post
     * DELETE /api/marketplace/posts/{postId}
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Response<String>> deletePost(
            @PathVariable UUID postId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] DELETE /api/marketplace/posts/{} - userId={}", correlationId, postId, userIdHeader);
        
        UUID userId = UUID.fromString(userIdHeader);
        Response<String> response = marketplaceService.deletePost(postId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Add a comment to a post
     * POST /api/marketplace/comments
     */
    @PostMapping("/comments")
    public ResponseEntity<Response<MarketplaceCommentResponse>> addComment(
            @RequestBody @Valid CreateCommentRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] POST /api/marketplace/comments - postId={}, userId={}", 
                   correlationId, request.postId(), userIdHeader);
        
        UUID userId = UUID.fromString(userIdHeader);
        Response<MarketplaceCommentResponse> response = marketplaceService.addComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete a comment
     * DELETE /api/marketplace/comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Response<String>> deleteComment(
            @PathVariable UUID commentId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] DELETE /api/marketplace/comments/{} - userId={}", correlationId, commentId, userIdHeader);
        
        UUID userId = UUID.fromString(userIdHeader);
        Response<String> response = marketplaceService.deleteComment(commentId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get comments for a post
     * GET /api/marketplace/posts/{postId}/comments
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Response<PaginatedResponse<MarketplaceCommentResponse>>> getPostComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] GET /api/marketplace/posts/{}/comments - pageNo={}, pageSize={}", 
                   correlationId, postId, pageNo, pageSize);
        
        Response<PaginatedResponse<MarketplaceCommentResponse>> response = 
                marketplaceService.getPostComments(postId, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }
}

