package com.resolveit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IncidentResolveDto {

    @NotBlank(message = "Resolution notes are required when resolving an incident")
    @Size(min = 10, max = 5000, message = "Resolution notes must be at least 10 characters")
    private String resolutionNotes;

    public IncidentResolveDto() {
    }

    public IncidentResolveDto(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
