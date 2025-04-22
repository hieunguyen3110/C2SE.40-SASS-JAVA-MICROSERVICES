package org.com.elearningservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.helper.CreateApiResponse;
import org.com.elearningservice.service.ELearningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cronjob")
public class CronJobController {
    private final ELearningService eLearningService;

    @PostMapping("/e-learning/account-analyze-data")
    public ApiResponse<List<AnalyzeData>> getAnalyzeData(@RequestBody List<Long> accountIds) throws Exception {
        return CreateApiResponse.createResponse(eLearningService.getListDataAnalyze(accountIds),false);
    }
}
