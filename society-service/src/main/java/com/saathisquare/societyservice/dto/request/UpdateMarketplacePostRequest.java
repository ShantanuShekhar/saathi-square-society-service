package com.saathisquare.societyservice.dto.request;

import com.saathisquare.societyservice.enums.PostStatus;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for updating a marketplace post
 */
public record UpdateMarketplacePostRequest(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        String description,

        Double price,

        Integer quantity,

        @Size(max = 100, message = "Location must not exceed 100 characters")
        String location,

        @Size(max = 100, message = "Contact info must not exceed 100 characters")
        String contactInfo,

        PostStatus status, // For marking as SOLD or CLOSED

        List<String> imageUrls // Updated image URLs
) {
}

