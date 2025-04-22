package org.com.elearningservice.service;

import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.dto.response.QuestionDTO;

import java.util.List;

public interface ELearningService {
    List<AnalyzeData> getListDataAnalyze(List<Long> accountIds) throws Exception;
    AnalyzeData getAnalyzeDataByAccountId(Long accountId) throws Exception;
    String saveQuestion(List<QuestionDTO> questionDTOS, Long subjectId) throws Exception;
}
