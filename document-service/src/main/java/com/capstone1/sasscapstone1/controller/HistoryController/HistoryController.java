package com.capstone1.sasscapstone1.controller.HistoryController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.service.HistoryService.HistoryService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    // Track document download
    @PostMapping("/track")
    public ApiResponse<String> trackDownload(@RequestParam Long docId, @RequestParam String username) {
        historyService.trackDownload(docId, username);
        return CreateApiResponse.createResponse("Download tracked successfully",false);
    }
}


