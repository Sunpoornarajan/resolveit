package com.resolveit.controller;

import com.resolveit.dto.CommentCreateDto;
import com.resolveit.dto.IncidentCreateDto;
import com.resolveit.dto.IncidentFilterDto;
import com.resolveit.entity.Incident;
import com.resolveit.entity.User;
import com.resolveit.enums.IncidentCategory;
import com.resolveit.enums.IncidentPriority;
import com.resolveit.enums.IncidentStatus;
import com.resolveit.service.CommentService;
import com.resolveit.service.DashboardService;
import com.resolveit.service.DepartmentService;
import com.resolveit.service.IncidentService;
import com.resolveit.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final DashboardService dashboardService;
    private final IncidentService incidentService;
    private final DepartmentService departmentService;
    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    public EmployeeController(DashboardService dashboardService,
                              IncidentService incidentService,
                              DepartmentService departmentService,
                              CommentService commentService,
                              SecurityUtils securityUtils) {
        this.dashboardService = dashboardService;
        this.incidentService = incidentService;
        this.departmentService = departmentService;
        this.commentService = commentService;
        this.securityUtils = securityUtils;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        model.addAttribute("activeNav", "employee");
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User current = securityUtils.getCurrentUser();
        model.addAttribute("stats", dashboardService.getEmployeeStats(current));
        model.addAttribute("recentIncidents", incidentService.findRecentForEmployee(current));
        model.addAttribute("pageTitle", "Employee Dashboard");
        return "employee/dashboard";
    }

    @GetMapping("/incidents")
    public String listIncidents(@ModelAttribute("filter") IncidentFilterDto filter, Model model) {
        User current = securityUtils.getCurrentUser();
        Page<Incident> incidentPage = incidentService.findMyIncidents(current, filter);

        model.addAttribute("incidents", incidentPage.getContent());
        model.addAttribute("page", incidentPage);
        model.addAttribute("filter", filter);
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("priorities", IncidentPriority.values());
        model.addAttribute("categories", IncidentCategory.values());
        model.addAttribute("pageTitle", "My Incidents");
        return "employee/incidents";
    }

    @GetMapping("/incidents/new")
    public String newIncidentForm(Model model) {
        if (!model.containsAttribute("incidentDto")) {
            IncidentCreateDto dto = new IncidentCreateDto();
            User current = securityUtils.getCurrentUser();
            if (current != null && current.getDepartment() != null) {
                dto.setDepartmentId(current.getDepartment().getId());
            }
            model.addAttribute("incidentDto", dto);
        }
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("categories", IncidentCategory.values());
        model.addAttribute("priorities", IncidentPriority.values());
        model.addAttribute("pageTitle", "Report an Incident");
        return "employee/incident-form";
    }

    @PostMapping("/incidents/new")
    public String createIncident(@Valid @ModelAttribute("incidentDto") IncidentCreateDto incidentDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.findAllActive());
            model.addAttribute("categories", IncidentCategory.values());
            model.addAttribute("priorities", IncidentPriority.values());
            return "employee/incident-form";
        }
        try {
            User current = securityUtils.getCurrentUser();
            Incident created = incidentService.createIncident(incidentDto, current);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Incident " + created.getIncidentNumber() + " submitted successfully! IT Support has been notified.");
            return "redirect:/employee/incidents/" + created.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("departments", departmentService.findAllActive());
            model.addAttribute("categories", IncidentCategory.values());
            model.addAttribute("priorities", IncidentPriority.values());
            return "employee/incident-form";
        }
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetail(@PathVariable Long id, Model model) {
        User current = securityUtils.getCurrentUser();
        Incident incident = incidentService.findById(id);
        incidentService.validateAccess(incident, current);

        model.addAttribute("incident", incident);
        model.addAttribute("comments", commentService.getCommentsForIncident(id));
        model.addAttribute("commentDto", new CommentCreateDto());
        model.addAttribute("pageTitle", "Incident: " + incident.getIncidentNumber());
        return "employee/incident-detail";
    }

    @PostMapping("/incidents/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentDto") CommentCreateDto commentDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment cannot be empty.");
            return "redirect:/employee/incidents/" + id;
        }
        try {
            User current = securityUtils.getCurrentUser();
            commentService.addComment(id, commentDto.getCommentText(), current);
            redirectAttributes.addFlashAttribute("successMessage", "Comment posted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/employee/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/close")
    public String closeIncident(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.closeIncident(id, current);
            redirectAttributes.addFlashAttribute("successMessage", "Resolution confirmed. Incident closed successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/employee/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/reopen")
    public String reopenIncident(@PathVariable Long id,
                                 @RequestParam(value = "reason", required = false) String reason,
                                 RedirectAttributes redirectAttributes) {
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.reopenIncident(id, reason, current);
            redirectAttributes.addFlashAttribute("successMessage", "Incident reopened for further support.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/employee/incidents/" + id;
    }
}
