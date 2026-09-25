package com.resolveit.controller;

import com.resolveit.dto.CommentCreateDto;
import com.resolveit.dto.IncidentFilterDto;
import com.resolveit.dto.IncidentResolveDto;
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
@RequestMapping("/support")
public class SupportController {

    private final DashboardService dashboardService;
    private final IncidentService incidentService;
    private final DepartmentService departmentService;
    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    public SupportController(DashboardService dashboardService,
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
        model.addAttribute("activeNav", "support");
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User current = securityUtils.getCurrentUser();
        model.addAttribute("stats", dashboardService.getSupportStats(current));
        model.addAttribute("recentIncidents", incidentService.findRecentForSupport(current));
        model.addAttribute("pageTitle", "IT Support Dashboard");
        return "support/dashboard";
    }

    @GetMapping("/incidents")
    public String listIncidents(@ModelAttribute("filter") IncidentFilterDto filter,
                                @RequestParam(value = "scope", defaultValue = "assigned") String scope,
                                Model model) {
        User current = securityUtils.getCurrentUser();
        Page<Incident> incidentPage;

        if ("all".equalsIgnoreCase(scope)) {
            // Queue view of all incidents
            incidentPage = incidentService.searchIncidents(filter);
        } else {
            // Incidents assigned to this engineer
            incidentPage = incidentService.findAssignedIncidents(current, filter);
        }

        model.addAttribute("incidents", incidentPage.getContent());
        model.addAttribute("page", incidentPage);
        model.addAttribute("filter", filter);
        model.addAttribute("scope", scope);
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("priorities", IncidentPriority.values());
        model.addAttribute("categories", IncidentCategory.values());
        model.addAttribute("pageTitle", "Assigned Incidents");
        return "support/incidents";
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetail(@PathVariable Long id, Model model) {
        Incident incident = incidentService.findById(id);

        model.addAttribute("incident", incident);
        model.addAttribute("comments", commentService.getCommentsForIncident(id));
        model.addAttribute("commentDto", new CommentCreateDto());
        model.addAttribute("resolveDto", new IncidentResolveDto());
        model.addAttribute("pageTitle", "Support Incident: " + incident.getIncidentNumber());
        return "support/incident-detail";
    }

    @PostMapping("/incidents/{id}/start-work")
    public String startWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.startWork(id, current);
            redirectAttributes.addFlashAttribute("successMessage", "Work started. Incident status updated to IN PROGRESS.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/support/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/resolve")
    public String resolveIncident(@PathVariable Long id,
                                  @Valid @ModelAttribute("resolveDto") IncidentResolveDto resolveDto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Resolution notes are required (at least 10 characters).");
            return "redirect:/support/incidents/" + id;
        }
        try {
            User current = securityUtils.getCurrentUser();
            incidentService.resolveIncident(id, resolveDto, current);
            redirectAttributes.addFlashAttribute("successMessage", "Incident resolved successfully with resolution notes!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/support/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentDto") CommentCreateDto commentDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment cannot be empty.");
            return "redirect:/support/incidents/" + id;
        }
        try {
            User current = securityUtils.getCurrentUser();
            commentService.addComment(id, commentDto.getCommentText(), current);
            redirectAttributes.addFlashAttribute("successMessage", "Support comment added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/support/incidents/" + id;
    }
}
