package org.com.batchservice.repository;

import org.com.batchservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface StudyGroupClient {
    @GetExchange("/cronjob/study-group/check-participate-group")
    ApiResponse<Boolean> checkAccountIsParticipateGroup(@RequestParam("accountId") Long accountId);
}
