package org.com.batchservice.repository;

import org.com.batchservice.dto.request.CheckFileRequest;
import org.com.batchservice.dto.request.UploadFileRequest;
import org.com.batchservice.dto.response.ApiResponse;
import org.com.batchservice.dto.response.CheckFileResponse;
import org.com.batchservice.dto.response.QuestionResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface ChatbotClient {
    @PostExchange("/check-file")
    ApiResponse<CheckFileResponse> checkFile(@RequestBody CheckFileRequest request);
    @PostExchange("/upload-file")
    ApiResponse<String> uploadFile(@RequestBody UploadFileRequest request);
    @GetExchange("/document/generate-question")
    ApiResponse<List<QuestionResponse>> generateQuestion(@RequestParam("docIds") List<Long> docIds);
}
