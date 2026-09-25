package com.resolveit.service.impl;

import com.resolveit.dto.DashboardStatsDto;
import com.resolveit.entity.User;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.enums.RoleType;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.repository.UserRepository;
import com.resolveit.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(IncidentRepository incidentRepository, UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardStatsDto getAdminStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalIncidents(incidentRepository.count());
        stats.setOpenIncidents(incidentRepository.countByStatus(IncidentStatus.OPEN));
        stats.setAssignedIncidents(incidentRepository.countByStatus(IncidentStatus.ASSIGNED));
        stats.setInProgressIncidents(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS));
        stats.setResolvedIncidents(incidentRepository.countByStatus(IncidentStatus.RESOLVED));
        stats.setClosedIncidents(incidentRepository.countByStatus(IncidentStatus.CLOSED));
        stats.setCriticalIncidents(incidentRepository.countByPriority(IncidentPriority.CRITICAL));

        stats.setTotalEmployees(userRepository.countByRole(RoleType.EMPLOYEE));
        stats.setTotalSupportEngineers(userRepository.countByRole(RoleType.IT_SUPPORT));

        return stats;
    }

    @Override
    public DashboardStatsDto getEmployeeStats(User employee) {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setMyTotalIncidents(incidentRepository.countByCreatedBy(employee));
        stats.setMyOpenIncidents(incidentRepository.countByCreatedByAndStatus(employee, IncidentStatus.OPEN));
        stats.setMyInProgressIncidents(incidentRepository.countByCreatedByAndStatus(employee, IncidentStatus.IN_PROGRESS));
        stats.setMyResolvedIncidents(incidentRepository.countByCreatedByAndStatus(employee, IncidentStatus.RESOLVED));
        stats.setMyClosedIncidents(incidentRepository.countByCreatedByAndStatus(employee, IncidentStatus.CLOSED));

        return stats;
    }

    @Override
    public DashboardStatsDto getSupportStats(User engineer) {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setMyTotalIncidents(incidentRepository.countByAssignedTo(engineer));
        stats.setMyOpenIncidents(incidentRepository.countByAssignedToAndStatus(engineer, IncidentStatus.ASSIGNED));
        stats.setMyInProgressIncidents(incidentRepository.countByAssignedToAndStatus(engineer, IncidentStatus.IN_PROGRESS));
        stats.setMyResolvedIncidents(incidentRepository.countByAssignedToAndStatus(engineer, IncidentStatus.RESOLVED));
        stats.setMyHighPriorityIncidents(incidentRepository.countByAssignedToAndPriorityAndStatusNot(engineer, IncidentPriority.HIGH, IncidentStatus.CLOSED));
        stats.setMyCriticalPriorityIncidents(incidentRepository.countByAssignedToAndPriorityAndStatusNot(engineer, IncidentPriority.CRITICAL, IncidentStatus.CLOSED));

        return stats;
    }
}
