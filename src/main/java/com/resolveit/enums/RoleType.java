package com.resolveit.enums;

public enum RoleType {
    ADMIN("Administrator"),
    EMPLOYEE("Employee"),
    IT_SUPPORT("IT Support Engineer");

    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
