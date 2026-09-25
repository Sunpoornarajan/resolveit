package com.resolveit.dto;

import com.resolveit.enums.IncidentPriority;
import jakarta.validation.constraints.NotNull;

public class IncidentAssignDto {

    @NotNull(message = "Support engineer must be selected")
    private Long assignedToId;

    private IncidentPriority priority;

    public IncidentAssignDto() {
    }

    public IncidentAssignDto(Long assignedToId, IncidentPriority priority) {
        this.assignedToId = assignedToId;
        this.priority = priority;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public IncidentPriority getPriority() {
        return priority;
    }

    public void setPriority(IncidentPriority priority) {
        this.priority = priority;
    }
}
