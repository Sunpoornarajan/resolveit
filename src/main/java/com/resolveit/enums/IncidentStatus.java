package com.resolveit.enums;

public enum IncidentStatus {
    OPEN("Open", "badge-status-open"),
    ASSIGNED("Assigned", "badge-status-assigned"),
    IN_PROGRESS("In Progress", "badge-status-in-progress"),
    RESOLVED("Resolved", "badge-status-resolved"),
    CLOSED("Closed", "badge-status-closed");

    private final String displayName;
    private final String badgeClass;

    IncidentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
