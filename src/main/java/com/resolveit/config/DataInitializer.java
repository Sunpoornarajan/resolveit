package com.resolveit.config;

import com.resolveit.entity.Department;
import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentComment;
import com.resolveit.entity.IncidentHistory;
import com.resolveit.entity.User;
import com.resolveit.enums.HistoryAction;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.enums.RoleType;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.repository.IncidentCommentRepository;
import com.resolveit.repository.IncidentHistoryRepository;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           IncidentRepository incidentRepository,
                           IncidentCommentRepository commentRepository,
                           IncidentHistoryRepository historyRepository,
                           PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data seed.");
            return;
        }

        log.info("Starting ResolveIT initial data seeding...");

        // 1. Seed Departments
        Department deptIT = departmentRepository.save(new Department("IT", "Information Technology and Systems"));
        Department deptHR = departmentRepository.save(new Department("HR", "Human Resources & Talent Acquisition"));
        Department deptFinance = departmentRepository.save(new Department("Finance", "Finance, Accounting and Payroll"));
        Department deptOps = departmentRepository.save(new Department("Operations", "Business Operations & Logistics"));
        Department deptSales = departmentRepository.save(new Department("Sales", "Sales and Client Relations"));
        Department deptAdmin = departmentRepository.save(new Department("Administration", "Executive Office and Facilities"));

        // 2. Seed Users (passwords: Admin@123, Support@123, Employee@123)
        User admin = new User(
                "EMP-1001",
                "System",
                "Administrator",
                "admin",
                "admin@resolveit.com",
                passwordEncoder.encode("Admin@123"),
                RoleType.ADMIN,
                deptIT
        );
        userRepository.save(admin);

        User support = new User(
                "EMP-1002",
                "Marcus",
                "Vance",
                "support",
                "support@resolveit.com",
                passwordEncoder.encode("Support@123"),
                RoleType.IT_SUPPORT,
                deptIT
        );
        userRepository.save(support);

        User supportAlex = new User(
                "EMP-1003",
                "Alex",
                "Chen",
                "alex_support",
                "alex.support@resolveit.com",
                passwordEncoder.encode("Support@123"),
                RoleType.IT_SUPPORT,
                deptIT
        );
        userRepository.save(supportAlex);

        User employee = new User(
                "EMP-1004",
                "David",
                "Miller",
                "employee",
                "employee@resolveit.com",
                passwordEncoder.encode("Employee@123"),
                RoleType.EMPLOYEE,
                deptFinance
        );
        userRepository.save(employee);

        User employeeSarah = new User(
                "EMP-1005",
                "Sarah",
                "Connor",
                "sarah_hr",
                "sarah.hr@resolveit.com",
                passwordEncoder.encode("Employee@123"),
                RoleType.EMPLOYEE,
                deptHR
        );
        userRepository.save(employeeSarah);

        // 3. Seed Realistic Incidents
        // Incident 1: OPEN (HIGH, VPN)
        Incident inc1 = new Incident(
                "INC-100001",
                "Cisco AnyConnect VPN Gateway Disconnects Periodically",
                "Every 15-20 minutes, the VPN connection drops with error 'Session timed out'. Affects ability to access internal finance databases.",
                IncidentCategory.VPN,
                IncidentPriority.HIGH,
                employee,
                deptFinance
        );
        inc1 = incidentRepository.save(inc1);
        historyRepository.save(new IncidentHistory(inc1, HistoryAction.CREATED, null, "OPEN", employee));

        // Incident 2: ASSIGNED (MEDIUM, HARDWARE)
        Incident inc2 = new Incident(
                "INC-100002",
                "Lenovo ThinkPad Screen Flickering After Display Driver Update",
                "Screen flashes black intermittently when connecting to external HDMI monitor. Rolled back drivers once but issue recurred.",
                IncidentCategory.HARDWARE,
                IncidentPriority.MEDIUM,
                employeeSarah,
                deptHR
        );
        inc2.setStatus(IncidentStatus.ASSIGNED);
        inc2.setAssignedTo(support);
        inc2 = incidentRepository.save(inc2);
        historyRepository.save(new IncidentHistory(inc2, HistoryAction.CREATED, null, "OPEN", employeeSarah));
        historyRepository.save(new IncidentHistory(inc2, HistoryAction.ASSIGNED, "Unassigned", support.getFullName(), admin));

        // Incident 3: IN_PROGRESS (CRITICAL, SERVER)
        Incident inc3 = new Incident(
                "INC-100003",
                "Core ERP Database Reporting Latency & Connection Pool Exhaustion",
                "Production warehouse management service is unable to acquire DB connection threads. Multiple checkout operations pending.",
                IncidentCategory.SERVER,
                IncidentPriority.CRITICAL,
                employee,
                deptOps
        );
        inc3.setStatus(IncidentStatus.IN_PROGRESS);
        inc3.setAssignedTo(supportAlex);
        inc3 = incidentRepository.save(inc3);
        historyRepository.save(new IncidentHistory(inc3, HistoryAction.CREATED, null, "OPEN", employee));
        historyRepository.save(new IncidentHistory(inc3, HistoryAction.ASSIGNED, "Unassigned", supportAlex.getFullName(), admin));
        historyRepository.save(new IncidentHistory(inc3, HistoryAction.STATUS_CHANGED, "ASSIGNED", "IN_PROGRESS", supportAlex));
        commentRepository.save(new IncidentComment(inc3, supportAlex, "Investigating connection pool sizing in HikariCP and slow running queries on the inventory table."));

        // Incident 4: RESOLVED (MEDIUM, SOFTWARE)
        Incident inc4 = new Incident(
                "INC-100004",
                "Microsoft 365 License Deactivation Alert on Outlook",
                "Outlook desktop client shows 'Unlicensed Product' banner. Web client works fine.",
                IncidentCategory.SOFTWARE,
                IncidentPriority.MEDIUM,
                employeeSarah,
                deptHR
        );
        inc4.setStatus(IncidentStatus.RESOLVED);
        inc4.setAssignedTo(support);
        inc4.setResolvedAt(LocalDateTime.now().minusHours(2));
        inc4.setResolutionNotes("Re-assigned the E5 license from Azure AD portal and purged local Office token cache. Outlook re-authenticated successfully.");
        inc4 = incidentRepository.save(inc4);
        historyRepository.save(new IncidentHistory(inc4, HistoryAction.CREATED, null, "OPEN", employeeSarah));
        historyRepository.save(new IncidentHistory(inc4, HistoryAction.ASSIGNED, "Unassigned", support.getFullName(), admin));
        historyRepository.save(new IncidentHistory(inc4, HistoryAction.STATUS_CHANGED, "ASSIGNED", "IN_PROGRESS", support));
        historyRepository.save(new IncidentHistory(inc4, HistoryAction.RESOLVED, "IN_PROGRESS", "RESOLVED", support));
        commentRepository.save(new IncidentComment(inc4, support, "Verified license assignment in Microsoft Entra Admin Center. Please restart Outlook."));
        commentRepository.save(new IncidentComment(inc4, employeeSarah, "Thank you, banner is gone now!"));

        // Incident 5: CLOSED (LOW, PASSWORD)
        Incident inc5 = new Incident(
                "INC-100005",
                "Password Reset Required for Training Portal Access",
                "Unable to login to compliance training portal. Reset email is not arriving in inbox.",
                IncidentCategory.PASSWORD,
                IncidentPriority.LOW,
                employee,
                deptAdmin
        );
        inc5.setStatus(IncidentStatus.CLOSED);
        inc5.setAssignedTo(support);
        inc5.setResolvedAt(LocalDateTime.now().minusDays(1));
        inc5.setClosedAt(LocalDateTime.now().minusHours(5));
        inc5.setResolutionNotes("Generated temporary OTP and updated employee's registered alternative email address. Employee logged in successfully.");
        inc5 = incidentRepository.save(inc5);
        historyRepository.save(new IncidentHistory(inc5, HistoryAction.CREATED, null, "OPEN", employee));
        historyRepository.save(new IncidentHistory(inc5, HistoryAction.ASSIGNED, "Unassigned", support.getFullName(), admin));
        historyRepository.save(new IncidentHistory(inc5, HistoryAction.STATUS_CHANGED, "ASSIGNED", "IN_PROGRESS", support));
        historyRepository.save(new IncidentHistory(inc5, HistoryAction.RESOLVED, "IN_PROGRESS", "RESOLVED", support));
        historyRepository.save(new IncidentHistory(inc5, HistoryAction.CLOSED, "RESOLVED", "CLOSED", employee));

        log.info("ResolveIT initial data seeding completed successfully!");
    }
}
