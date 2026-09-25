package com.resolveit.service;

import com.resolveit.dto.IncidentAssignDto;
import com.resolveit.dto.IncidentCreateDto;
import com.resolveit.dto.IncidentFilterDto;
import com.resolveit.dto.IncidentResolveDto;
import com.resolveit.entity.Incident;
import com.resolveit.entity.User;
import com.resolveit.enums.IncidentPriority;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IncidentService {

    Incident createIncident(IncidentCreateDto dto, User creator);

    Incident findById(Long id);

    Incident findByIncidentNumber(String incidentNumber);

    Page<Incident> searchIncidents(IncidentFilterDto filter);

    Page<Incident> findMyIncidents(User employee, IncidentFilterDto filter);

    Page<Incident> findAssignedIncidents(User engineer, IncidentFilterDto filter);

    Incident assignIncident(Long incidentId, IncidentAssignDto dto, User assignedBy);

    Incident updatePriority(Long incidentId, IncidentPriority newPriority, User updatedBy);

    Incident startWork(Long incidentId, User engineer);

    Incident resolveIncident(Long incidentId, IncidentResolveDto dto, User engineer);

    Incident closeIncident(Long incidentId, User closedBy);

    Incident reopenIncident(Long incidentId, String reason, User reopenedBy);

    List<Incident> findRecentIncidents();

    List<Incident> findRecentForEmployee(User employee);

    List<Incident> findRecentForSupport(User engineer);

    void validateAccess(Incident incident, User user);
}
