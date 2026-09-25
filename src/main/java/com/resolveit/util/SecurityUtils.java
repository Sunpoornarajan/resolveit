package com.resolveit.util;

import com.resolveit.entity.User;
import com.resolveit.security.CustomUserDetails;
import com.resolveit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserService userService;

    public SecurityUtils(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userService.findById(userDetails.getId());
        }

        return userService.findByUsername(auth.getName());
    }
}
