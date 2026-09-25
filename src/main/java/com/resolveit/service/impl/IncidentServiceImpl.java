package com.resolveit.service.impl;

import com.resolveit.dto.IncidentAssignDto;
import com.resolveit.dto.IncidentCreateDto;
import com.resolveit.dto.IncidentFilterDto;
import com.resolveit.dto.IncidentResolveDto;
import com.resolveit.entity.Department;
import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentComment;
import com.resolveit.entity.IncidentHistory;
import com.resolveit.entity.User;
import com.resolveit.enums.HistoryAction;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.enums.RoleType;
import com.resolveit.exception.InvalidStatusTransitionException;
import com.resolveit.exception.ResourceNotFoundException;
import com.resolveit.exception.UnauthorizedAccessException;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.repository.IncidentHistoryRepository;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.repository.UserRepository;
import com.resolveit.service.IncidentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public IncidentServiceImpl(IncidentRepository incidentRepository,
                               IncidentHistoryRepository historyRepository,
                               UserRepository userRepository,
                               DepartmentRepository departmentRepository) {
        this.incidentRepository = incidentRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    private synchronized String generateNextIncidentNumber() {
        Long maxId = incidentRepository.findTopByOrderByIdDesc()
                .map(Incident::getId)
                .orElse(0L);
        long nextSeq = 100001L + maxId;
        return "INC-" + nextSeq;
    }

    @Override
    public Incident createIncident(IncidentCreateDto dto, User creator) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        String incidentNumber = generateNextIncidentNumber();

        Incident incident = new Incident();
        incident.setIncidentNumber(incidentNumber);
        incident.setTitle(dto.getTitle().trim());
        incident.setDescription(dto.getDescription().trim());
        incident.setCategory(dto.getCategory());
        incident.setPriority(dto.getPriority() != null ? dto.getPriority() : IncidentPriority.MEDIUM);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedBy(creator);
        incident.setDepartment(department);

        Incident saved = incidentRepository.save(incident);

        // Record history
        IncidentHistory history = new IncidentHistory(
                saved,
                HistoryAction.CREATED,
                null,
                IncidentStatus.OPEN.name(),
                creator
        );
        historyRepository.save(history);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Incident findById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Incident findByIncidentNumber(String incidentNumber) {
        return incidentRepository.findByIncidentNumber(incidentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with number: " + incidentNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Incident> searchIncidents(IncidentFilterDto filter) {
        Specification<Incident> spec = buildSpecification(filter);
        Pageable pageable = createPageable(filter);
        return incidentRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Incident> findMyIncidents(User employee, IncidentFilterDto filter) {
        filter.setCreatedById(employee.getId());
        return searchIncidents(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Incident> findAssignedIncidents(User engineer, IncidentFilterDto filter) {
        filter.setAssignedToId(engineer.getId());
        return searchIncidents(filter);
    }

    private Specification<Incident> buildSpecification(IncidentFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIncidentNumber() != null && !filter.getIncidentNumber().trim().isEmpty()) {
                predicates.add(cb.like(cb.upper(root.get("incidentNumber")), "%" + filter.getIncidentNumber().trim().toUpperCase() + "%"));
            }

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String term = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), term);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), term);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }

            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (filter.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), filter.getDepartmentId()));
            }

            if (filter.getAssignedToId() != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), filter.getAssignedToId()));
            }

            if (filter.getCreatedById() != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCreatedById()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable createPageable(IncidentFilterDto filter) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.getSortBy() != null ? filter.getSortBy() : "createdAt"
        );
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    @Override
    public Incident assignIncident(Long incidentId, IncidentAssignDto dto, User assignedBy) {
        Incident incident = findById(incidentId);

        if (incident.getStatus() == IncidentStatus.CLOSED) {
            throw new InvalidStatusTransitionException("Cannot assign a closed incident. Reopen it first.");
        }

        User engineer = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Engineer not found with ID: " + dto.getAssignedToId()));

        if (engineer.getRole() != RoleType.IT_SUPPORT && engineer.getRole() != RoleType.ADMIN) {
            throw new IllegalArgumentException("Incidents can only be assigned to IT Support Engineers or Administrators");
        }

        String prevAssigned = incident.getAssignedTo() != null ? incident.getAssignedTo().getFullName() : "Unassigned";
        incident.setAssignedTo(engineer);

        // Transition from OPEN to ASSIGNED if currently OPEN
        HistoryAction action = HistoryAction.ASSIGNED;
        if (incident.getStatus() == IncidentStatus.OPEN) {
            incident.setStatus(IncidentStatus.ASSIGNED);
        } else {
            action = HistoryAction.REASSIGNED;
        }

        // Adjust priority if specified
        if (dto.getPriority() != null && dto.getPriority() != incident.getPriority()) {
            IncidentPriority oldPriority = incident.getPriority();
            incident.setPriority(dto.getPriority());
            historyRepository.save(new IncidentHistory(
                    incident,
                    HistoryAction.PRIORITY_CHANGED,
                    oldPriority.name(),
                    dto.getPriority().name(),
                    assignedBy
            ));
        }

        Incident saved = incidentRepository.save(incident);

        historyRepository.save(new IncidentHistory(
                saved,
                action,
                prevAssigned,
                engineer.getFullName(),
                assignedBy
        ));

        return saved;
    }

    @Override
    public Incident updatePriority(Long incidentId, IncidentPriority newPriority, User updatedBy) {
        Incident incident = findById(incidentId);
        if (incident.getStatus() == IncidentStatus.CLOSED) {
            throw new InvalidStatusTransitionException("Cannot change priority of a closed incident.");
        }

        if (incident.getPriority() != newPriority) {
            IncidentPriority old = incident.getPriority();
            incident.setPriority(newPriority);
            Incident saved = incidentRepository.save(incident);

            historyRepository.save(new IncidentHistory(
                    saved,
                    HistoryAction.PRIORITY_CHANGED,
                    old.name(),
                    newPriority.name(),
                    updatedBy
            ));
            return saved;
        }
        return incident;
    }

    @Override
    public Incident startWork(Long incidentId, User engineer) {
        Incident incident = findById(incidentId);

        // Validate state transition: ASSIGNED -> IN_PROGRESS
        if (incident.getStatus() != IncidentStatus.ASSIGNED && incident.getStatus() != IncidentStatus.OPEN) {
            throw new InvalidStatusTransitionException("Cannot start work on incident in status: " + incident.getStatus());
        }

        // Auto-assign to current engineer if not assigned
        if (incident.getAssignedTo() == null) {
            incident.setAssignedTo(engineer);
        }

        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(IncidentStatus.IN_PROGRESS);

        Incident saved = incidentRepository.save(incident);

        historyRepository.save(new IncidentHistory(
                saved,
                HistoryAction.STATUS_CHANGED,
                oldStatus.name(),
                IncidentStatus.IN_PROGRESS.name(),
                engineer
        ));

        return saved;
    }

    @Override
    public Incident resolveIncident(Long incidentId, IncidentResolveDto dto, User engineer) {
        Incident incident = findById(incidentId);

        // Workflow transition: must be in IN_PROGRESS (or ASSIGNED)
        if (incident.getStatus() != IncidentStatus.IN_PROGRESS && incident.getStatus() != IncidentStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Incident must be IN_PROGRESS before it can be resolved. Current status: " + incident.getStatus());
        }

        if (dto.getResolutionNotes() == null || dto.getResolutionNotes().trim().length() < 10) {
            throw new IllegalArgumentException("Resolution notes must be at least 10 characters long.");
        }

        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolutionNotes(dto.getResolutionNotes().trim());
        incident.setResolvedAt(LocalDateTime.now());

        if (incident.getAssignedTo() == null) {
            incident.setAssignedTo(engineer);
        }

        Incident saved = incidentRepository.save(incident);

        historyRepository.save(new IncidentHistory(
                saved,
                HistoryAction.RESOLVED,
                oldStatus.name(),
                IncidentStatus.RESOLVED.name(),
                engineer
        ));

        return saved;
    }

    @Override
    public Incident closeIncident(Long incidentId, User closedBy) {
        Incident incident = findById(incidentId);

        // Workflow transition: must be RESOLVED before closing
        if (incident.getStatus() != IncidentStatus.RESOLVED) {
            throw new InvalidStatusTransitionException("Only RESOLVED incidents can be closed. Current status: " + incident.getStatus());
        }

        // Only creator or admin can close
        if (closedBy.getRole() != RoleType.ADMIN && !incident.getCreatedBy().getId().equals(closedBy.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to close this incident. Only the creator or an Administrator can close it.");
        }

        incident.setStatus(IncidentStatus.CLOSED);
        incident.setClosedAt(LocalDateTime.now());

        Incident saved = incidentRepository.save(incident);

        historyRepository.save(new IncidentHistory(
                saved,
                HistoryAction.CLOSED,
                IncidentStatus.RESOLVED.name(),
                IncidentStatus.CLOSED.name(),
                closedBy
        ));

        return saved;
    }

    @Override
    public Incident reopenIncident(Long incidentId, String reason, User reopenedBy) {
        Incident incident = findById(incidentId);

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            // Reopen RESOLVED -> IN_PROGRESS
            incident.setStatus(IncidentStatus.IN_PROGRESS);
            incident.setResolvedAt(null);
            Incident saved = incidentRepository.save(incident);

            historyRepository.save(new IncidentHistory(
                    saved,
                    HistoryAction.REOPENED,
                    IncidentStatus.RESOLVED.name(),
                    IncidentStatus.IN_PROGRESS.name() + (reason != null && !reason.isBlank() ? " (" + reason.trim() + ")" : ""),
                    reopenedBy
            ));
            return saved;
        } else if (incident.getStatus() == IncidentStatus.CLOSED) {
            // Admin only reopen CLOSED -> OPEN
            if (reopenedBy.getRole() != RoleType.ADMIN) {
                throw new UnauthorizedAccessException("Only Administrators can reopen a closed incident.");
            }
            incident.setStatus(IncidentStatus.OPEN);
            incident.setClosedAt(null);
            incident.setResolvedAt(null);
            Incident saved = incidentRepository.save(incident);

            historyRepository.save(new IncidentHistory(
                    saved,
                    HistoryAction.REOPENED,
                    IncidentStatus.CLOSED.name(),
                    IncidentStatus.OPEN.name() + (reason != null && !reason.isBlank() ? " (" + reason.trim() + ")" : ""),
                    reopenedBy
            ));
            return saved;
        } else {
            throw new InvalidStatusTransitionException("Cannot reopen incident in status: " + incident.getStatus());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> findRecentIncidents() {
        return incidentRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> findRecentForEmployee(User employee) {
        return incidentRepository.findTop5ByCreatedByOrderByCreatedAtDesc(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> findRecentForSupport(User engineer) {
        return incidentRepository.findTop5ByAssignedToOrderByCreatedAtDesc(engineer);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateAccess(Incident incident, User user) {
        if (user.getRole() == RoleType.ADMIN) {
            return;
        }
        if (user.getRole() == RoleType.IT_SUPPORT) {
            return;
        }
        if (user.getRole() == RoleType.EMPLOYEE) {
            if (!incident.getCreatedBy().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to view or modify this incident.");
            }
        }
    }
}
