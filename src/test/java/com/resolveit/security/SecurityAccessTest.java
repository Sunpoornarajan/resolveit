package com.resolveit.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated user accessing /admin/dashboard should be redirected to /login")
    void testUnauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Employee role accessing /admin/dashboard should be blocked with 403 Forbidden")
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void testEmployee_AccessAdminDashboard_Forbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin role accessing /admin/dashboard should succeed with 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdmin_AccessAdminDashboard_Success() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Employee role accessing /employee/dashboard should succeed with 200 OK")
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void testEmployee_AccessEmployeeDashboard_Success() throws Exception {
        mockMvc.perform(get("/employee/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT Support role accessing /support/dashboard should succeed with 200 OK")
    @WithMockUser(username = "support", roles = {"IT_SUPPORT"})
    void testSupport_AccessSupportDashboard_Success() throws Exception {
        mockMvc.perform(get("/support/dashboard"))
                .andExpect(status().isOk());
    }
}
