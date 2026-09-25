package com.resolveit.controller;

import com.resolveit.dto.*;
import com.resolveit.entity.Department;
import com.resolveit.entity.Incident;
import com.resolveit.entity.User;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.enums.RoleType;
import com.resolveit.service.*;
import com.resolveit.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final IncidentService incidentService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final CommentService commentService;
    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    public AdminController(DashboardService dashboardService,
                           IncidentService incidentService,
                           UserService userService,
                           DepartmentService departmentService,
                           CommentService commentService,
                           ReportService reportService,
                           SecurityUtils securityUtils) {
        this.dashboardService = dashboardService;
        this.incidentService = incidentService;
        this.userService = userService;
        this.departmentService = departmentService;
        this.commentService = commentService;
        this.reportService = reportService;
        this.securityUtils = securityUtils;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        model.addAttribute("activeNav", "admin");
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getAdminStats());
        model.addAttribute("recentIncidents", incidentService.findRecentIncidents());
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
    }

    // User Management
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("pageTitle", "User Management");
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        if (!model.containsAttribute("userDto")) {
            model.addAttribute("userDto", new UserCreateDto());
        }
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("roles", RoleType.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Create User");
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    public String createUser(@Valid @ModelAttribute("userDto") UserCreateDto userDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.findAllActive());
            model.addAttribute("roles", RoleType.values());
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }
        try {
            userService.createUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + userDto.getUsername() + "' created successfully!");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("departments", departmentService.findAllActive());
            model.addAttribute("roles", RoleType.values());
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        UserEditDto dto = new UserEditDto();
        dto.setId(user.getId());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
        dto.setActive(user.isActive());

        model.addAttribute("userDto", dto);
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("roles", RoleType.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit User: " + user.getUsername());
        return "admin/user-form";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("userDto") UserEditDto userDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.findAllActive());
            model.addAttribute("roles", RoleType.values());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        }
        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("departments", departmentService.findAllActive());
            model.addAttribute("roles", RoleType.values());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        }
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully!");
        return "redirect:/admin/users";
    }

    // Department Management
    @GetMapping("/departments")
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("pageTitle", "Department Management");
        return "admin/departments";
    }

    @GetMapping("/departments/new")
    public String newDepartmentForm(Model model) {
        if (!model.containsAttribute("deptDto")) {
            model.addAttribute("deptDto", new DepartmentDto());
        }
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Create Department");
        return "admin/department-form";
    }

    @PostMapping("/departments/new")
    public String createDepartment(@Valid @ModelAttribute("deptDto") DepartmentDto deptDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/department-form";
        }
        try {
            departmentService.createDepartment(deptDto);
            redirectAttributes.addFlashAttribute("successMessage", "Department '" + deptDto.getName() + "' created successfully!");
            return "redirect:/admin/departments";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/department-form";
        }
    }

    @GetMapping("/departments/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        Department dept = departmentService.findById(id);
        DepartmentDto dto = new DepartmentDto(dept.getId(), dept.getName(), dept.getDescription(), dept.isActive());
        model.addAttribute("deptDto", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit Department: " + dept.getName());
        return "admin/department-form";
    }

    @PostMapping("/departments/edit/{id}")
    public String updateDepartment(@PathVariable Long id,
                                   @Valid @ModelAttribute("deptDto") DepartmentDto deptDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/department-form";
        }
        try {
            departmentService.updateDepartment(id, deptDto);
            redirectAttributes.addFlashAttribute("successMessage", "Department updated successfully!");
            return "redirect:/admin/departments";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isEdit", true);
            return "admin/department-form";
        }
    }

    @PostMapping("/departments/toggle-status/{id}")
    public String toggleDepartmentStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        departmentService.toggleDepartmentStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Department status updated successfully!");
        return "redirect:/admin/departments";
    }

    // Incidents Management
    @GetMapping("/incidents")
    public String listIncidents(@ModelAttribute("filter") IncidentFilterDto filter, Model model) {
        Page<Incident> incidentPage = incidentService.searchIncidents(filter);

        model.addAttribute("incidents", incidentPage.getContent());
        model.addAttribute("page", incidentPage);
        model.addAttribute("filter", filter);
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("supportEngineers", userService.findActiveSupportEngineers());
        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("priorities", IncidentPriority.values());
        model.addAttribute("categories", IncidentCategory.values());
        model.addAttribute("pageTitle", "Incident Management");
        return "admin/incidents";
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetail(@PathVariable Long id, Model model) {
        Incident incident = incidentService.findById(id);
        model.addAttribute("incident", incident);
        model.addAttribute("comments", commentService.getCommentsForIncident(id));
        model.addAttribute("supportEngineers", userService.findActiveSupportEngineers());
        model.addAttribute("priorities", IncidentPriority.values());
        model.addAttribute("commentDto", new CommentCreateDto());
        model.addAttribute("assignDto", new IncidentAssignDto());
        model.addAttribute("pageTitle", "Incident: " + incident.getIncidentNumber());
        return "admin/incident-detail";
    }

    @PostMapping("/incidents/{id}/assign")
    public String assignIncident(@PathVariable Long id,
                                 @ModelAttribute IncidentAssignDto assignDto,
                                 RedirectAttributes redirectAttributes) {
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.assignIncident(id, assignDto, current);
            redirectAttributes.addFlashAttribute("successMessage", "Incident successfully assigned!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/priority")
    public String updatePriority(@PathVariable Long id,
                                 @RequestParam("priority") IncidentPriority priority,
                                 RedirectAttributes redirectAttributes) {
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.updatePriority(id, priority, current);
            redirectAttributes.addFlashAttribute("successMessage", "Priority updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentDto") CommentCreateDto commentDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment cannot be empty.");
            return "redirect:/admin/incidents/" + id;
        }
        try {
            User current = securityUtils.getCurrentUser();
            commentService.addComment(id, commentDto.getCommentText(), current);
            redirectAttributes.addFlashAttribute("successMessage", "Comment posted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/reopen")
    public String reopenIncident(@PathVariable Long id,
                                 @RequestParam(value = "reason", required = false) String reason,
                                 RedirectAttributes redirectAttributes) {
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.reopenIncident(id, reason, current);
            redirectAttributes.addFlashAttribute("successMessage", "Incident reopened successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/incidents/" + id;
    }

    // Reports
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("reportStats", reportService.getReportStats());
        model.addAttribute("pageTitle", "Reports & Analytics");
        return "admin/reports";
    }
}
