package com.saathisquare.societyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating a comment on a marketplace post
 */
public record CreateCommentRequest(
        @NotNull(message = "Post ID is required")
        UUID postId,

        @NotBlank(message = "Comment content is required")
        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String content
) {
}

