package com.saathisquare.societyservice.enums;

/**
 * Status of marketplace post
 */
public enum PostStatus {
    ACTIVE("Active"),
    SOLD("Sold"),
    CLOSED("Closed"),
    DELETED("Deleted");

    private final String displayName;

    PostStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

