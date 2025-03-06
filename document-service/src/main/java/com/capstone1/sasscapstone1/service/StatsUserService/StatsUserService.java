package com.capstone1.sasscapstone1.service.StatsUserService;

import com.capstone1.sasscapstone1.dto.AccountStatisticsDto.AccountStatisticsDto;
import com.capstone1.sasscapstone1.dto.AdminDashboardStatsDto.StatsDto;

public interface StatsUserService {
    StatsDto getDashboardStats();
    AccountStatisticsDto countAccountStatics(long accountId) throws Exception;
}
