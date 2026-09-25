package com.resolveit.service.impl;

import com.resolveit.dto.ReportStatsDto;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final IncidentRepository incidentRepository;

    public ReportServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public ReportStatsDto getReportStats() {
        ReportStatsDto dto = new ReportStatsDto();
        dto.setTotalIncidents(incidentRepository.count());

        // Status breakdown
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (IncidentStatus s : IncidentStatus.values()) {
            statusMap.put(s.getDisplayName(), 0L);
        }
        List<Object[]> statusCounts = incidentRepository.countGroupedByStatus();
        for (Object[] row : statusCounts) {
            if (row[0] != null) {
                IncidentStatus status = (IncidentStatus) row[0];
                Long count = (Long) row[1];
                statusMap.put(status.getDisplayName(), count);
            }
        }
        dto.setIncidentsByStatus(statusMap);

        // Priority breakdown
        Map<String, Long> priorityMap = new LinkedHashMap<>();
        for (IncidentPriority p : IncidentPriority.values()) {
            priorityMap.put(p.getDisplayName(), 0L);
        }
        List<Object[]> priorityCounts = incidentRepository.countGroupedByPriority();
        for (Object[] row : priorityCounts) {
            if (row[0] != null) {
                IncidentPriority priority = (IncidentPriority) row[0];
                Long count = (Long) row[1];
                priorityMap.put(priority.getDisplayName(), count);
            }
        }
        dto.setIncidentsByPriority(priorityMap);

        // Category breakdown
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (IncidentCategory c : IncidentCategory.values()) {
            categoryMap.put(c.getDisplayName(), 0L);
        }
        List<Object[]> categoryCounts = incidentRepository.countGroupedByCategory();
        for (Object[] row : categoryCounts) {
            if (row[0] != null) {
                IncidentCategory category = (IncidentCategory) row[0];
                Long count = (Long) row[1];
                categoryMap.put(category.getDisplayName(), count);
            }
        }
        dto.setIncidentsByCategory(categoryMap);

        // Department breakdown
        Map<String, Long> deptMap = new LinkedHashMap<>();
        List<Object[]> deptCounts = incidentRepository.countGroupedByDepartment();
        for (Object[] row : deptCounts) {
            if (row[0] != null) {
                String deptName = (String) row[0];
                Long count = (Long) row[1];
                deptMap.put(deptName, count);
            }
        }
        dto.setIncidentsByDepartment(deptMap);

        // Engineer breakdown
        Map<String, Long> engineerMap = new LinkedHashMap<>();
        List<Object[]> engineerCounts = incidentRepository.countGroupedByAssignedEngineer();
        for (Object[] row : engineerCounts) {
            if (row[0] != null && row[1] != null) {
                String engineerName = row[0] + " " + row[1];
                Long count = (Long) row[2];
                engineerMap.put(engineerName, count);
            }
        }
        dto.setIncidentsByEngineer(engineerMap);

        // Monthly count breakdown
        Map<String, Long> monthMap = new LinkedHashMap<>();
        try {
            List<Object[]> monthCounts = incidentRepository.countGroupedByMonth();
            for (Object[] row : monthCounts) {
                if (row[0] != null && row[1] != null) {
                    String monthYear = String.format("%04d-%02d", ((Number) row[0]).intValue(), ((Number) row[1]).intValue());
                    Long count = ((Number) row[2]).longValue();
                    monthMap.put(monthYear, count);
                }
            }
        } catch (Exception ignored) {
            // In case DB dialect differs in testing
        }
        dto.setMonthlyIncidentCounts(monthMap);

        return dto;
    }
}
