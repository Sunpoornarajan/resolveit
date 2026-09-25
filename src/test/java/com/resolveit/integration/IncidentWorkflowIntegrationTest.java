package com.resolveit.integration;

import com.resolveit.dto.IncidentAssignDto;
import com.resolveit.dto.IncidentCreateDto;
import com.resolveit.dto.IncidentResolveDto;
import com.resolveit.entity.Department;
import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentComment;
import com.resolveit.entity.IncidentHistory;
import com.resolveit.entity.User;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.enums.RoleType;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.repository.UserRepository;
import com.resolveit.service.CommentService;
import com.resolveit.service.IncidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IncidentWorkflowIntegrationTest {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private com.resolveit.repository.IncidentHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("End-to-End Workflow: Create -> Assign -> Start Work -> Comment -> Resolve -> Close")
    void testCompleteIncidentLifecycle() {
        // 1. Prepare Department and Users
        Department hrDept = departmentRepository.save(new Department("HR Operations", "Human Resources"));

        User employee = userRepository.save(new User(
                "EMP-TEST-1", "Alice", "Smith", "alice_test", "alice@testcorp.com",
                "pass123", RoleType.EMPLOYEE, hrDept
        ));

        User support = userRepository.save(new User(
                "EMP-TEST-2", "Bob", "Support", "bob_support_test", "bob@testcorp.com",
                "pass123", RoleType.IT_SUPPORT, hrDept
        ));

        User admin = userRepository.save(new User(
                "EMP-TEST-3", "Charlie", "Admin", "charlie_admin_test", "charlie@testcorp.com",
                "pass123", RoleType.ADMIN, hrDept
        ));

        // Step 1: Employee reports incident
        IncidentCreateDto createDto = new IncidentCreateDto();
        createDto.setTitle("Cannot access internal HR portal");
        createDto.setDescription("Received HTTP 500 error when clicking on employee benefits link.");
        createDto.setCategory(IncidentCategory.SOFTWARE);
        createDto.setPriority(IncidentPriority.HIGH);
        createDto.setDepartmentId(hrDept.getId());

        Incident incident = incidentService.createIncident(createDto, employee);

        assertNotNull(incident.getId());
        assertTrue(incident.getIncidentNumber().startsWith("INC-"));
        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertEquals(IncidentPriority.HIGH, incident.getPriority());
        assertEquals(employee.getId(), incident.getCreatedBy().getId());

        // Step 2: Admin assigns to IT Support
        IncidentAssignDto assignDto = new IncidentAssignDto(support.getId(), IncidentPriority.HIGH);
        incident = incidentService.assignIncident(incident.getId(), assignDto, admin);

        assertEquals(IncidentStatus.ASSIGNED, incident.getStatus());
        assertEquals(support.getId(), incident.getAssignedTo().getId());

        // Step 3: IT Support starts work
        incident = incidentService.startWork(incident.getId(), support);
        assertEquals(IncidentStatus.IN_PROGRESS, incident.getStatus());

        // Step 4: Collaboration via comments
        commentService.addComment(incident.getId(), "Checking web server logs and database pool status.", support);
        commentService.addComment(incident.getId(), "Thank you, still observing the issue on Chrome.", employee);

        List<IncidentComment> comments = commentService.getCommentsForIncident(incident.getId());
        assertEquals(2, comments.size());

        // Step 5: IT Support resolves incident with resolution notes
        IncidentResolveDto resolveDto = new IncidentResolveDto("Restarted the HR portal application container and restored connection pool.");
        incident = incidentService.resolveIncident(incident.getId(), resolveDto, support);

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertNotNull(incident.getResolvedAt());
        assertNotNull(incident.getResolutionNotes());

        // Step 6: Employee confirms and closes incident
        incident = incidentService.closeIncident(incident.getId(), employee);

        assertEquals(IncidentStatus.CLOSED, incident.getStatus());
        assertNotNull(incident.getClosedAt());

        // Step 7: Verify final audit trail in database
        List<IncidentHistory> histories = historyRepository.findByIncidentOrderByTimestampDesc(incident);
        assertFalse(histories.isEmpty());
        assertTrue(histories.size() >= 5); // CREATED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, etc.
    }
}
