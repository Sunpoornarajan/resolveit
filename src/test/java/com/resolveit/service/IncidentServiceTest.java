package com.resolveit.service;

import com.resolveit.dto.IncidentAssignDto;
import com.resolveit.dto.IncidentCreateDto;
import com.resolveit.dto.IncidentResolveDto;
import com.resolveit.entity.Department;
import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentHistory;
import com.resolveit.entity.User;
import com.resolveit.enums.HistoryAction;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.enums.RoleType;
import com.resolveit.exception.InvalidStatusTransitionException;
import com.resolveit.exception.UnauthorizedAccessException;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.repository.IncidentHistoryRepository;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.repository.UserRepository;
import com.resolveit.service.impl.IncidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private User employee;
    private User supportEngineer;
    private User admin;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        department.setId(1L);

        employee = new User("EMP-1001", "David", "Miller", "david", "david@test.com", "pass", RoleType.EMPLOYEE, department);
        employee.setId(101L);

        supportEngineer = new User("EMP-1002", "Marcus", "Vance", "marcus", "marcus@test.com", "pass", RoleType.IT_SUPPORT, department);
        supportEngineer.setId(102L);

        admin = new User("EMP-1003", "Admin", "User", "admin", "admin@test.com", "pass", RoleType.ADMIN, department);
        admin.setId(103L);
    }

    @Test
    @DisplayName("Should successfully create an incident with generated INC number and OPEN status")
    void testCreateIncident_Success() {
        IncidentCreateDto dto = new IncidentCreateDto();
        dto.setTitle("VPN connection drops");
        dto.setDescription("Detailed description of VPN dropping every few minutes.");
        dto.setCategory(IncidentCategory.VPN);
        dto.setPriority(IncidentPriority.HIGH);
        dto.setDepartmentId(1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(incidentRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident inc = invocation.getArgument(0);
            inc.setId(1L);
            return inc;
        });

        Incident created = incidentService.createIncident(dto, employee);

        assertNotNull(created);
        assertEquals("INC-100001", created.getIncidentNumber());
        assertEquals("VPN connection drops", created.getTitle());
        assertEquals(IncidentStatus.OPEN, created.getStatus());
        assertEquals(IncidentPriority.HIGH, created.getPriority());
        assertEquals(employee, created.getCreatedBy());

        verify(historyRepository, times(1)).save(any(IncidentHistory.class));
    }

    @Test
    @DisplayName("Should successfully assign an incident to IT support engineer and transition to ASSIGNED")
    void testAssignIncident_Success() {
        Incident incident = new Incident("INC-100001", "Printer issue", "Paper jam", IncidentCategory.PRINTER, IncidentPriority.LOW, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.OPEN);

        IncidentAssignDto dto = new IncidentAssignDto(102L, IncidentPriority.MEDIUM);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(userRepository.findById(102L)).thenReturn(Optional.of(supportEngineer));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident updated = incidentService.assignIncident(1L, dto, admin);

        assertEquals(supportEngineer, updated.getAssignedTo());
        assertEquals(IncidentStatus.ASSIGNED, updated.getStatus());
        assertEquals(IncidentPriority.MEDIUM, updated.getPriority());

        verify(historyRepository, atLeastOnce()).save(any(IncidentHistory.class));
    }

    @Test
    @DisplayName("Should reject assigning an incident to a non-support employee")
    void testAssignIncident_InvalidRole_ThrowsException() {
        Incident incident = new Incident("INC-100001", "Printer issue", "Paper jam", IncidentCategory.PRINTER, IncidentPriority.LOW, employee, department);
        incident.setId(1L);

        User anotherEmployee = new User("EMP-1005", "Bob", "Smith", "bob", "bob@test.com", "pass", RoleType.EMPLOYEE, department);
        anotherEmployee.setId(105L);

        IncidentAssignDto dto = new IncidentAssignDto(105L, null);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(userRepository.findById(105L)).thenReturn(Optional.of(anotherEmployee));

        assertThrows(IllegalArgumentException.class, () -> incidentService.assignIncident(1L, dto, admin));
    }

    @Test
    @DisplayName("Should transition incident from ASSIGNED to IN_PROGRESS when work begins")
    void testStartWork_Success() {
        Incident incident = new Incident("INC-100001", "Hardware fail", "Monitor broken", IncidentCategory.HARDWARE, IncidentPriority.MEDIUM, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.ASSIGNED);
        incident.setAssignedTo(supportEngineer);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident updated = incidentService.startWork(1L, supportEngineer);

        assertEquals(IncidentStatus.IN_PROGRESS, updated.getStatus());
        verify(historyRepository, times(1)).save(any(IncidentHistory.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to start work on an incident that is already RESOLVED")
    void testStartWork_InvalidTransition_ThrowsException() {
        Incident incident = new Incident("INC-100001", "Hardware fail", "Monitor broken", IncidentCategory.HARDWARE, IncidentPriority.MEDIUM, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.RESOLVED);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(InvalidStatusTransitionException.class, () -> incidentService.startWork(1L, supportEngineer));
    }

    @Test
    @DisplayName("Should successfully resolve an incident with valid resolution notes")
    void testResolveIncident_Success() {
        Incident incident = new Incident("INC-100001", "Email bounce", "Quota exceeded", IncidentCategory.EMAIL, IncidentPriority.MEDIUM, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.IN_PROGRESS);
        incident.setAssignedTo(supportEngineer);

        IncidentResolveDto dto = new IncidentResolveDto("Cleaned up mailbox archive and increased quota to 50GB.");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident resolved = incidentService.resolveIncident(1L, dto, supportEngineer);

        assertEquals(IncidentStatus.RESOLVED, resolved.getStatus());
        assertEquals("Cleaned up mailbox archive and increased quota to 50GB.", resolved.getResolutionNotes());
        assertNotNull(resolved.getResolvedAt());
        verify(historyRepository, times(1)).save(any(IncidentHistory.class));
    }

    @Test
    @DisplayName("Should reject resolution when resolution notes are too short")
    void testResolveIncident_MissingNotes_ThrowsException() {
        Incident incident = new Incident("INC-100001", "Email bounce", "Quota exceeded", IncidentCategory.EMAIL, IncidentPriority.MEDIUM, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.IN_PROGRESS);

        IncidentResolveDto dto = new IncidentResolveDto("Fixed"); // Less than 10 characters

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalArgumentException.class, () -> incidentService.resolveIncident(1L, dto, supportEngineer));
    }

    @Test
    @DisplayName("Should close a resolved incident when confirmed by the reporting employee")
    void testCloseIncident_Success() {
        Incident incident = new Incident("INC-100001", "Software bug", "Cannot export PDF", IncidentCategory.SOFTWARE, IncidentPriority.LOW, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolutionNotes("Reinstalled PDF export printer driver.");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident closed = incidentService.closeIncident(1L, employee);

        assertEquals(IncidentStatus.CLOSED, closed.getStatus());
        assertNotNull(closed.getClosedAt());
        verify(historyRepository, times(1)).save(any(IncidentHistory.class));
    }

    @Test
    @DisplayName("Should reject closing a resolved incident by an unauthorized third-party employee")
    void testCloseIncident_UnauthorizedUser_ThrowsException() {
        Incident incident = new Incident("INC-100001", "Software bug", "Cannot export PDF", IncidentCategory.SOFTWARE, IncidentPriority.LOW, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.RESOLVED);

        User stranger = new User("EMP-1099", "Eve", "Hacker", "eve", "eve@test.com", "pass", RoleType.EMPLOYEE, department);
        stranger.setId(999L);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(UnauthorizedAccessException.class, () -> incidentService.closeIncident(1L, stranger));
    }

    @Test
    @DisplayName("Should allow creator to reopen a RESOLVED incident back to IN_PROGRESS")
    void testReopenIncident_ResolvedToInProgress() {
        Incident incident = new Incident("INC-100001", "VPN issue", "Cannot login", IncidentCategory.VPN, IncidentPriority.HIGH, employee, department);
        incident.setId(1L);
        incident.setStatus(IncidentStatus.RESOLVED);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident reopened = incidentService.reopenIncident(1L, "Still dropping every 10 minutes", employee);

        assertEquals(IncidentStatus.IN_PROGRESS, reopened.getStatus());
        assertNull(reopened.getResolvedAt());
        verify(historyRepository, times(1)).save(any(IncidentHistory.class));
    }
}
