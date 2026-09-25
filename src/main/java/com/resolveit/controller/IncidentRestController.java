package com.resolveit.controller;

import com.resolveit.dto.DashboardStatsDto;
import com.resolveit.dto.IncidentFilterDto;
import com.resolveit.dto.IncidentResponseDto;
import com.resolveit.entity.Incident;
import com.resolveit.service.DashboardService;
import com.resolveit.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Incident Management", description = "REST APIs for querying IT incidents and operational metrics")
public class IncidentRestController {

    private final IncidentService incidentService;
    private final DashboardService dashboardService;

    public IncidentRestController(IncidentService incidentService,
                                  DashboardService dashboardService) {
        this.incidentService = incidentService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/incidents")
    @Operation(summary = "Search and list incidents", description = "Retrieves a paginated list of incidents based on optional search and filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved incident page")
    })
    public ResponseEntity<Page<IncidentResponseDto>> getIncidents(
            @ModelAttribute IncidentFilterDto filter) {
        Page<Incident> incidentPage = incidentService.searchIncidents(filter);
        Page<IncidentResponseDto> dtoPage = incidentPage.map(IncidentResponseDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/incidents/{id}")
    @Operation(summary = "Get incident by ID", description = "Retrieves complete incident details for a specific database ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident found"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> getIncidentById(
            @Parameter(description = "Internal database ID of the incident") @PathVariable Long id) {
        Incident incident = incidentService.findById(id);
        return ResponseEntity.ok(IncidentResponseDto.fromEntity(incident));
    }

    @GetMapping("/incidents/number/{incidentNumber}")
    @Operation(summary = "Get incident by human-readable incident number", description = "Retrieves an incident by its formal identifier (e.g. INC-100001).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident found"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponseDto> getIncidentByNumber(
            @Parameter(description = "Incident number (e.g. INC-100001)") @PathVariable String incidentNumber) {
        Incident incident = incidentService.findByIncidentNumber(incidentNumber);
        return ResponseEntity.ok(IncidentResponseDto.fromEntity(incident));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get operational metrics", description = "Returns high-level statistics across all incident pipelines.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operational metrics retrieved")
    })
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getAdminStats());
    }
}
