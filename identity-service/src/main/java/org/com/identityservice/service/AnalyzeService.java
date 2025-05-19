package org.com.identityservice.service;

import org.com.identityservice.dto.response.MessageAnalyze;

public interface AnalyzeService {
    String updateAnalyzeMessage(MessageAnalyze request) throws Exception;
}
