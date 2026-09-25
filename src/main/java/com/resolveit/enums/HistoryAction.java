package com.resolveit.enums;

public enum HistoryAction {
    CREATED("Incident Created"),
    ASSIGNED("Incident Assigned"),
    REASSIGNED("Incident Reassigned"),
    PRIORITY_CHANGED("Priority Changed"),
    STATUS_CHANGED("Status Changed"),
    COMMENT_ADDED("Comment Added"),
    RESOLVED("Incident Resolved"),
    CLOSED("Incident Closed"),
    REOPENED("Incident Reopened");

    private final String displayName;

    HistoryAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
