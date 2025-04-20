package org.com.identityservice.service;

import org.com.identityservice.dto.request.UpdateMessageAnalyzeRequest;

public interface AnalyzeService {
    String updateAnalyzeMessage(UpdateMessageAnalyzeRequest request) throws Exception;
}
