package org.com.batchservice.repository;

import org.com.batchservice.dto.request.CheckFileRequest;
import org.com.batchservice.dto.request.UploadFileRequest;
import org.com.batchservice.dto.response.ApiResponse;
import org.com.batchservice.dto.response.CheckFileResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface ChatbotClient {
    @PostExchange("/check-file")
    ApiResponse<CheckFileResponse> checkFile(@RequestBody CheckFileRequest request);
    @PostExchange("/upload-file")
    ApiResponse<String> uploadFile(@RequestBody UploadFileRequest request);
}
