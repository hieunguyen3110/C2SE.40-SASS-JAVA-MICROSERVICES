package org.com.elearningservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.helper.CreateApiResponse;
import org.com.elearningservice.service.ELearningService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ELearningController {
    private final ELearningService eLearningService;

    @GetMapping("/analyze-data")
    public ApiResponse<AnalyzeData> getAnalyzeDataByAccountId(@RequestParam("accountId") Long accountId) throws Exception {
        return CreateApiResponse.createResponse(eLearningService.getAnalyzeDataByAccountId(accountId),false);
    }
}
