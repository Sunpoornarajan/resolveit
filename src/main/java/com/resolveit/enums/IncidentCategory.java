package com.resolveit.enums;

public enum IncidentCategory {
    HARDWARE("Hardware", "Physical devices, laptops, monitors, peripherals"),
    SOFTWARE("Software", "Applications, OS issues, installation, licenses"),
    NETWORK("Network", "Wi-Fi, Ethernet, LAN/WAN connectivity issues"),
    EMAIL("Email", "Outlook, mailbox quotas, delivery, spam issues"),
    VPN("VPN", "Remote access, Cisco AnyConnect, VPN disconnects"),
    PASSWORD("Password", "Account lockout, password reset, credential issues"),
    PRINTER("Printer", "Network printers, paper jams, driver configuration"),
    SERVER("Server", "Internal servers, VMs, host unreachable, database"),
    ACCESS_REQUEST("Access Request", "Folder permissions, system access, AD groups"),
    OTHER("Other", "General inquiries, miscellaneous IT requests");

    private final String displayName;
    private final String description;

    IncidentCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
