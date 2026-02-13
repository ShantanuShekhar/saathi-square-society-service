package com.saathisquare.societyservice.service.impl;

import com.saathisquare.societyservice.service.BatchUserLookupService;
import com.saathisquare.societyservice.dto.request.CreateCommentRequest;
import com.saathisquare.societyservice.dto.request.CreateMarketplacePostRequest;
import com.saathisquare.societyservice.dto.request.MarketplacePostFilterRequest;
import com.saathisquare.societyservice.dto.request.UpdateMarketplacePostRequest;
import com.saathisquare.societyservice.dto.response.MarketplaceCommentResponse;
import com.saathisquare.societyservice.dto.response.MarketplaceImageResponse;
import com.saathisquare.societyservice.dto.response.MarketplacePostResponse;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.dto.response.UserDetailsResponse;
import com.saathisquare.societyservice.enums.PostStatus;
import com.saathisquare.societyservice.exception.BusinessException;
import com.saathisquare.societyservice.exception.ResourceNotFoundException;
import com.saathisquare.societyservice.model.MarketplaceComment;
import com.saathisquare.societyservice.model.MarketplaceImage;
import com.saathisquare.societyservice.model.MarketplacePost;
import com.saathisquare.societyservice.model.Society;
import com.saathisquare.societyservice.repository.MarketplaceCommentRepository;
import com.saathisquare.societyservice.repository.MarketplaceImageRepository;
import com.saathisquare.societyservice.repository.MarketplacePostRepository;
import com.saathisquare.societyservice.repository.SocietyRepository;
import com.saathisquare.societyservice.service.MarketplaceService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketplaceServiceImpl implements MarketplaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceServiceImpl.class);

    private final MarketplacePostRepository postRepository;
    private final MarketplaceImageRepository imageRepository;
    private final MarketplaceCommentRepository commentRepository;
    private final SocietyRepository societyRepository;
    private final BatchUserLookupService batchUserLookupService;

    @Override
    @Transactional
    public Response<MarketplacePostResponse> createPost(CreateMarketplacePostRequest request, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Creating marketplace post: title={}, type={}, userId={}", 
                   correlationId, request.title(), request.postType(), userId);

        // Validate society exists
        Society society = societyRepository.findById(request.societyId())
                .orElseThrow(() -> new ResourceNotFoundException("Society not found with id: " + request.societyId()));

        // Validate price for SELL and GROUP_BUY posts
        if ((request.postType() == com.saathisquare.societyservice.enums.PostType.SELL 
             || request.postType() == com.saathisquare.societyservice.enums.PostType.GROUP_BUY) 
            && request.price() == null) {
            throw new BusinessException("INVALID_POST_DATA", "Price is required for " + request.postType() + " posts");
        }

        // Create post
        MarketplacePost post = MarketplacePost.builder()
                .title(request.title())
                .description(request.description())
                .postType(request.postType())
                .status(PostStatus.ACTIVE)
                .price(request.price())
                .quantity(request.quantity())
                .location(request.location())
                .contactInfo(request.contactInfo())
                .society(society)
                .createdBy(userId)
                .build();

        post = postRepository.save(post);
        LOGGER.info("[{}] Marketplace post created successfully: postId={}", correlationId, post.getPostId());

        // Add images if provided
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<MarketplaceImage> images = new java.util.ArrayList<>();
            for (int i = 0; i < request.imageUrls().size(); i++) {
                images.add(MarketplaceImage.builder()
                        .imageUrl(request.imageUrls().get(i))
                        .displayOrder(i)
                        .post(post)
                        .build());
            }
            imageRepository.saveAll(images);
        }

        return buildPostResponse(post);
    }

    @Override
    @Transactional
    public Response<MarketplacePostResponse> updatePost(UUID postId, UpdateMarketplacePostRequest request, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Updating marketplace post: postId={}, userId={}", correlationId, postId, userId);

        MarketplacePost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check ownership
        if (!post.getCreatedBy().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only update your own posts");
        }

        // Update fields
        if (request.title() != null) post.setTitle(request.title());
        if (request.description() != null) post.setDescription(request.description());
        if (request.price() != null) post.setPrice(request.price());
        if (request.quantity() != null) post.setQuantity(request.quantity());
        if (request.location() != null) post.setLocation(request.location());
        if (request.contactInfo() != null) post.setContactInfo(request.contactInfo());
        if (request.status() != null) {
            post.setStatus(request.status());
            if (request.status() == PostStatus.SOLD) {
                post.setSoldAt(LocalDateTime.now());
            }
        }

        post = postRepository.save(post);

        // Update images if provided
        if (request.imageUrls() != null) {
            // Delete existing images
            imageRepository.deleteByPostPostId(postId);
            // Add new images
            if (!request.imageUrls().isEmpty()) {
                List<MarketplaceImage> images = new java.util.ArrayList<>();
                for (int i = 0; i < request.imageUrls().size(); i++) {
                    images.add(MarketplaceImage.builder()
                            .imageUrl(request.imageUrls().get(i))
                            .displayOrder(i)
                            .post(post)
                            .build());
                }
                imageRepository.saveAll(images);
            }
        }

        LOGGER.info("[{}] Marketplace post updated successfully: postId={}", correlationId, postId);
        return buildPostResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<MarketplacePostResponse> getPostById(UUID postId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Fetching marketplace post: postId={}", correlationId, postId);

        MarketplacePost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getStatus() == PostStatus.DELETED) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        return buildPostResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PaginatedResponse<MarketplacePostResponse>> getFilteredPosts(MarketplacePostFilterRequest request) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Fetching filtered marketplace posts: societyId={}, type={}, status={}, searchTerm={}", 
                   correlationId, request.societyId(), request.postType(), request.status(), request.searchTerm());

        Pageable pageable = PageRequest.of(request.pageNo() - 1, request.pageSize());

        Page<MarketplacePost> posts = postRepository.findBySocietyIdWithFilters(
                request.societyId(),
                request.postType(),
                request.status(),
                request.searchTerm(),
                pageable);

        List<MarketplacePostResponse> postResponses = posts.getContent().stream()
                .map(this::buildPostResponseWithoutComments)
                .map(Response::getData)
                .collect(Collectors.toList());

        PaginatedResponse<MarketplacePostResponse> paginatedResponse = new PaginatedResponse<>(
                postResponses,
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.getNumber() + 1,
                posts.getSize()
        );

        Response<PaginatedResponse<MarketplacePostResponse>> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(paginatedResponse);

        return response;
    }

    @Override
    @Transactional
    public Response<MarketplacePostResponse> markAsSold(UUID postId, UUID soldToUserId, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Marking post as sold: postId={}, soldToUserId={}, userId={}", 
                   correlationId, postId, soldToUserId, userId);

        MarketplacePost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check ownership or admin permission
        if (!post.getCreatedBy().equals(userId)) {
            // TODO: Check if user is admin of the society
            throw new BusinessException("UNAUTHORIZED", "You can only mark your own posts as sold");
        }

        post.setStatus(PostStatus.SOLD);
        post.setSoldAt(LocalDateTime.now());
        post.setSoldTo(soldToUserId);
        post = postRepository.save(post);

        LOGGER.info("[{}] Post marked as sold successfully: postId={}", correlationId, postId);
        return buildPostResponse(post);
    }

    @Override
    @Transactional
    public Response<String> deletePost(UUID postId, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Deleting marketplace post: postId={}, userId={}", correlationId, postId, userId);

        MarketplacePost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getCreatedBy().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only delete your own posts");
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);

        Response<String> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage("Post deleted successfully");
        response.setData(postId.toString());

        LOGGER.info("[{}] Post deleted successfully: postId={}", correlationId, postId);
        return response;
    }

    @Override
    @Transactional
    public Response<MarketplaceCommentResponse> addComment(CreateCommentRequest request, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Adding comment to post: postId={}, userId={}", correlationId, request.postId(), userId);

        MarketplacePost post = postRepository.findById(request.postId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + request.postId()));

        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException("INVALID_POST", "Cannot comment on deleted post");
        }

        MarketplaceComment comment = MarketplaceComment.builder()
                .content(request.content())
                .createdBy(userId)
                .post(post)
                .isDeleted(false)
                .build();

        comment = commentRepository.save(comment);
        LOGGER.info("[{}] Comment added successfully: commentId={}", correlationId, comment.getCommentId());

        return buildCommentResponse(comment);
    }

    @Override
    @Transactional
    public Response<String> deleteComment(UUID commentId, UUID userId) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Deleting comment: commentId={}, userId={}", correlationId, commentId, userId);

        MarketplaceComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getCreatedBy().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only delete your own comments");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);

        Response<String> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage("Comment deleted successfully");
        response.setData(commentId.toString());

        LOGGER.info("[{}] Comment deleted successfully: commentId={}", correlationId, commentId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PaginatedResponse<MarketplaceCommentResponse>> getPostComments(UUID postId, int pageNo, int pageSize) {
        String correlationId = MDC.get("correlationId");
        LOGGER.info("[{}] Fetching comments for post: postId={}", correlationId, postId);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        List<MarketplaceComment> comments = commentRepository.findByPostPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);

        // Manual pagination (since we need all for ordering, then paginate)
        int start = (pageNo - 1) * pageSize;
        int end = Math.min(start + pageSize, comments.size());
        List<MarketplaceComment> paginatedComments = comments.subList(start, end);

        List<MarketplaceCommentResponse> commentResponses = paginatedComments.stream()
                .map(this::buildCommentResponse)
                .map(Response::getData)
                .collect(Collectors.toList());

        PaginatedResponse<MarketplaceCommentResponse> paginatedResponse = new PaginatedResponse<>(
                commentResponses,
                comments.size(),
                (int) Math.ceil((double) comments.size() / pageSize),
                pageNo,
                pageSize
        );

        Response<PaginatedResponse<MarketplaceCommentResponse>> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(paginatedResponse);

        return response;
    }

    // Helper methods
    private Response<MarketplacePostResponse> buildPostResponse(MarketplacePost post) {
        Response<MarketplacePostResponse> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(buildPostResponseWithoutComments(post).getData());
        return response;
    }

    /**
     * Batch load images for multiple posts to prevent N+1 queries
     */
    private Map<UUID, List<MarketplaceImage>> batchLoadImages(List<UUID> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<MarketplaceImage> allImages = imageRepository.findByPostPostIdIn(postIds);
        return allImages.stream()
                .collect(Collectors.groupingBy(image -> image.getPost().getPostId()));
    }

    /**
     * Optimized version of toPostResponse that uses pre-loaded data
     */
    private MarketplacePostResponse toPostResponseOptimized(
            MarketplacePost post, 
            List<MarketplaceImage> images, 
            Map<UUID, String> userNameMap) {
        
        if (images == null) {
            images = Collections.emptyList();
        }
        
        List<MarketplaceImageResponse> imageResponses = images.stream()
                .map(img -> new MarketplaceImageResponse(
                        img.getImageId(),
                        img.getImageUrl(),
                        img.getFileName(),
                        img.getFileSize(),
                        img.getContentType(),
                        img.getDisplayOrder()
                ))
                .collect(Collectors.toList());

        long commentCount = commentRepository.countByPostPostIdAndIsDeletedFalse(post.getPostId());
        String createdByName = userNameMap.getOrDefault(post.getCreatedBy(), "Resident");

        return new MarketplacePostResponse(
                post.getPostId(),
                post.getTitle(),
                post.getDescription(),
                post.getPostType(),
                post.getStatus(),
                post.getPrice(),
                post.getQuantity(),
                post.getLocation(),
                post.getContactInfo(),
                post.getSociety().getSocietyId(),
                post.getSociety().getName(),
                post.getCreatedBy(),
                createdByName,
                imageResponses,
                null, // Comments loaded separately
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getSoldAt(),
                post.getSoldTo()
        );
    }

    private Response<MarketplacePostResponse> buildPostResponseWithoutComments(MarketplacePost post) {
        // Use optimized version
        List<MarketplaceImage> images = imageRepository.findByPostPostIdOrderByDisplayOrderAsc(post.getPostId());
        String createdByName = batchUserLookupService.getUserNames(Set.of(post.getCreatedBy()))
                .getOrDefault(post.getCreatedBy(), "Resident");

        List<MarketplaceImageResponse> imageResponses = images.stream()
                .map(img -> new MarketplaceImageResponse(
                        img.getImageId(),
                        img.getImageUrl(),
                        img.getFileName(),
                        img.getFileSize(),
                        img.getContentType(),
                        img.getDisplayOrder()
                ))
                .collect(Collectors.toList());

        long commentCount = commentRepository.countByPostPostIdAndIsDeletedFalse(post.getPostId());
        String societyName = post.getSociety().getName();

        MarketplacePostResponse postResponse = new MarketplacePostResponse(
                post.getPostId(),
                post.getTitle(),
                post.getDescription(),
                post.getPostType(),
                post.getStatus(),
                post.getPrice(),
                post.getQuantity(),
                post.getLocation(),
                post.getContactInfo(),
                post.getSociety().getSocietyId(),
                societyName,
                post.getCreatedBy(),
                createdByName,
                imageResponses,
                null,
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getSoldAt(),
                post.getSoldTo()
        );

        Response<MarketplacePostResponse> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
        response.setData(postResponse);
        return response;
    }

    private Response<MarketplaceCommentResponse> buildCommentResponse(MarketplaceComment comment) {
        String createdByName = batchUserLookupService.getUserNames(Set.of(comment.getCreatedBy()))
                .getOrDefault(comment.getCreatedBy(), "Resident");

        MarketplaceCommentResponse commentResponse = new MarketplaceCommentResponse(
                comment.getCommentId(),
                comment.getContent(),
                comment.getCreatedBy(),
                createdByName,
                comment.getPost().getPostId(),
                comment.getCreatedAt()
        );

        Response<MarketplaceCommentResponse> response = new Response<>();
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage("Comment retrieved successfully");
        response.setData(commentResponse);
        return response;
    }

    private String getUserName(UUID userId) {
        return batchUserLookupService.getUserName(userId);
    }
}

