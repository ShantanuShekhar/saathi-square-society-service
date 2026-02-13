package com.saathisquare.societyservice.dto.request;

import com.saathisquare.societyservice.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a marketplace post
 */
public record CreateMarketplacePostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        String description, // Optional

        @NotNull(message = "Post type is required")
        PostType postType,

        UUID societyId, // Society ID the post belongs to

        Double price, // Required for SELL and GROUP_BUY

        Integer quantity, // Optional, mainly for GROUP_BUY

        @Size(max = 100, message = "Location must not exceed 100 characters")
        String location,

        @Size(max = 100, message = "Contact info must not exceed 100 characters")
        String contactInfo,

        List<String> imageUrls // URLs of uploaded images (handled separately in upload endpoint)
) {
}

