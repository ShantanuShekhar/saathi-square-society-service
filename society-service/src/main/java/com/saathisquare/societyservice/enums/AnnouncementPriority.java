package com.saathisquare.societyservice.enums;

/**
 * Priority level of announcement
 */
public enum AnnouncementPriority {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent");

    private final String displayName;

    AnnouncementPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

