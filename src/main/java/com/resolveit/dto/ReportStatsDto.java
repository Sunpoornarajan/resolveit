package com.resolveit.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportStatsDto {

    private Map<String, Long> incidentsByStatus = new LinkedHashMap<>();
    private Map<String, Long> incidentsByPriority = new LinkedHashMap<>();
    private Map<String, Long> incidentsByCategory = new LinkedHashMap<>();
    private Map<String, Long> incidentsByDepartment = new LinkedHashMap<>();
    private Map<String, Long> incidentsByEngineer = new LinkedHashMap<>();
    private Map<String, Long> monthlyIncidentCounts = new LinkedHashMap<>();

    private long totalIncidents;

    public ReportStatsDto() {
    }

    public Map<String, Long> getIncidentsByStatus() {
        return incidentsByStatus;
    }

    public void setIncidentsByStatus(Map<String, Long> incidentsByStatus) {
        this.incidentsByStatus = incidentsByStatus;
    }

    public Map<String, Long> getIncidentsByPriority() {
        return incidentsByPriority;
    }

    public void setIncidentsByPriority(Map<String, Long> incidentsByPriority) {
        this.incidentsByPriority = incidentsByPriority;
    }

    public Map<String, Long> getIncidentsByCategory() {
        return incidentsByCategory;
    }

    public void setIncidentsByCategory(Map<String, Long> incidentsByCategory) {
        this.incidentsByCategory = incidentsByCategory;
    }

    public Map<String, Long> getIncidentsByDepartment() {
        return incidentsByDepartment;
    }

    public void setIncidentsByDepartment(Map<String, Long> incidentsByDepartment) {
        this.incidentsByDepartment = incidentsByDepartment;
    }

    public Map<String, Long> getIncidentsByEngineer() {
        return incidentsByEngineer;
    }

    public void setIncidentsByEngineer(Map<String, Long> incidentsByEngineer) {
        this.incidentsByEngineer = incidentsByEngineer;
    }

    public Map<String, Long> getMonthlyIncidentCounts() {
        return monthlyIncidentCounts;
    }

    public void setMonthlyIncidentCounts(Map<String, Long> monthlyIncidentCounts) {
        this.monthlyIncidentCounts = monthlyIncidentCounts;
    }

    public long getTotalIncidents() {
        return totalIncidents;
    }

    public void setTotalIncidents(long totalIncidents) {
        this.totalIncidents = totalIncidents;
    }
}
