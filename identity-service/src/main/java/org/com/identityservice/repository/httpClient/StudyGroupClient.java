package org.com.identityservice.repository.httpClient;

import org.com.identityservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface StudyGroupClient {
    @GetExchange("/cronjob/study-group/check-participate-group")
    ApiResponse<Boolean> checkAccountIsParticipateGroup(@RequestParam("accountId") Long accountId);
}
