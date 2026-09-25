package com.resolveit.repository;

import com.resolveit.entity.Department;
import com.resolveit.entity.Incident;
import com.resolveit.entity.User;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    Optional<Incident> findByIncidentNumber(String incidentNumber);

    Optional<Incident> findTopByOrderByIdDesc();

    // Counts for Dashboards
    long countByStatus(IncidentStatus status);

    long countByPriority(IncidentPriority priority);

    long countByCreatedBy(User user);

    long countByCreatedByAndStatus(User user, IncidentStatus status);

    long countByAssignedTo(User user);

    long countByAssignedToAndStatus(User user, IncidentStatus status);

    long countByAssignedToAndPriority(User user, IncidentPriority priority);

    long countByAssignedToAndPriorityAndStatusNot(User user, IncidentPriority priority, IncidentStatus status);

    // Queries for lists
    Page<Incident> findByCreatedByOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Incident> findByAssignedToOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Incident> findTop5ByOrderByCreatedAtDesc();

    List<Incident> findTop5ByCreatedByOrderByCreatedAtDesc(User user);

    List<Incident> findTop5ByAssignedToOrderByCreatedAtDesc(User user);

    // Aggregations for Reports
    @Query("SELECT i.status as status, COUNT(i) as count FROM Incident i GROUP BY i.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT i.priority as priority, COUNT(i) as count FROM Incident i GROUP BY i.priority")
    List<Object[]> countGroupedByPriority();

    @Query("SELECT i.category as category, COUNT(i) as count FROM Incident i GROUP BY i.category")
    List<Object[]> countGroupedByCategory();

    @Query("SELECT i.department.name as departmentName, COUNT(i) as count FROM Incident i GROUP BY i.department.name")
    List<Object[]> countGroupedByDepartment();

    @Query("SELECT i.assignedTo.firstName, i.assignedTo.lastName, COUNT(i) as count FROM Incident i WHERE i.assignedTo IS NOT NULL GROUP BY i.assignedTo.id, i.assignedTo.firstName, i.assignedTo.lastName")
    List<Object[]> countGroupedByAssignedEngineer();

    @Query("SELECT FUNCTION('YEAR', i.createdAt), FUNCTION('MONTH', i.createdAt), COUNT(i) FROM Incident i GROUP BY FUNCTION('YEAR', i.createdAt), FUNCTION('MONTH', i.createdAt) ORDER BY FUNCTION('YEAR', i.createdAt) DESC, FUNCTION('MONTH', i.createdAt) DESC")
    List<Object[]> countGroupedByMonth();
}
