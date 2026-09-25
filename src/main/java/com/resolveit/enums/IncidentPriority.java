package com.resolveit.enums;

public enum IncidentPriority {
    LOW("Low", "badge-priority-low"),
    MEDIUM("Medium", "badge-priority-medium"),
    HIGH("High", "badge-priority-high"),
    CRITICAL("Critical", "badge-priority-critical");

    private final String displayName;
    private final String badgeClass;

    IncidentPriority(String displayName, String badgeClass) {
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
