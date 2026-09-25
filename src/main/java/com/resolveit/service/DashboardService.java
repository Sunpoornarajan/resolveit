package com.resolveit.service;

import com.resolveit.dto.DashboardStatsDto;
import com.resolveit.entity.User;

public interface DashboardService {

    DashboardStatsDto getAdminStats();

    DashboardStatsDto getEmployeeStats(User employee);

    DashboardStatsDto getSupportStats(User engineer);
}
