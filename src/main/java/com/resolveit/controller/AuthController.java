package com.resolveit.controller;

import com.resolveit.entity.User;
import com.resolveit.enums.RoleType;
import com.resolveit.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final SecurityUtils securityUtils;

    public AuthController(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @GetMapping("/")
    public String rootRedirect() {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == RoleType.ADMIN) {
            return "redirect:/admin/dashboard";
        } else if (user.getRole() == RoleType.IT_SUPPORT) {
            return "redirect:/support/dashboard";
        } else {
            return "redirect:/employee/dashboard";
        }
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        User user = securityUtils.getCurrentUser();
        if (user != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username/email or password, or account is disabled.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }

        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("title", "Access Denied");
        return "error/403";
    }
}
