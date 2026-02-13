package com.saathisquare.societyservice.enums;

/**
 * Type of marketplace post
 */
public enum PostType {
    SELL("Sell Item"),
    REQUEST("Request Item"),
    GROUP_BUY("Group Buy");

    private final String displayName;

    PostType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

