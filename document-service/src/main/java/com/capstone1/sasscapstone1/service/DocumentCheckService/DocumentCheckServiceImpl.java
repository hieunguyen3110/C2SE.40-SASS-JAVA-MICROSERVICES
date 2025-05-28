package com.capstone1.sasscapstone1.service.DocumentCheckService;

import com.capstone1.sasscapstone1.dto.CheckFileResponse.CheckFileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.httpClient.ChatbotClient;
import com.capstone1.sasscapstone1.request.CheckFileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentCheckServiceImpl implements DocumentCheckService {

    private final DocumentsRepository documentsRepository;
    private final ChatbotClient chatbotClient;

    @Override
    public void checkDocument(Long docId) {
        try {
            // Lấy tài liệu từ database
            Documents document = documentsRepository.findById(docId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document not found with ID: " + docId));
            if(document.getIsCheck()){
                throw new Exception("Tài liệu đã được check");
            }
            // Đọc nội dung file
            CheckFileResponse response= chatbotClient.checkFile(CheckFileRequest.builder()
                    .filePath(document.getFilePath())
                    .build()).getData();
            if((!response.isContainsSensitiveWords() && response.getSensitiveWords()==null) ||
                    (response.isContainsSensitiveWords() && response.getSensitiveWords().size()<10)){
                document.setIsCheck(true);
                documentsRepository.save(document);
            }else{
                throw new Exception("File chứa từ nhạy cảm quá nhiều!");
            }


        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Document error: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }
}
