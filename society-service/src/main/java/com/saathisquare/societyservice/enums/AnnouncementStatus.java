package com.saathisquare.societyservice.enums;

/**
 * Status of announcement lifecycle
 */
public enum AnnouncementStatus {
    DRAFT("Draft"),                    // Created but not published
    SCHEDULED("Scheduled"),            // Scheduled for future publication
    ACTIVE("Active"),                  // Currently visible to residents
    EXPIRED("Expired"),                // Past expiration date
    ARCHIVED("Archived");              // Manually archived

    private final String displayName;

    AnnouncementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

