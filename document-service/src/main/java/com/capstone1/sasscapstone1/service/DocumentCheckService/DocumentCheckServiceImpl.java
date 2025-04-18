package com.capstone1.sasscapstone1.service.DocumentCheckService;

import com.capstone1.sasscapstone1.dto.CheckFileResponse.CheckFileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
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
    private final RestTemplate restTemplate;
    @Value("${chatbot.url}")
    private String chatbotUrl;

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
            HttpHeaders httpHeaders= new HttpHeaders();
            Map<String,String> request= new HashMap<>();
            request.put("filePath",document.getFilePath());
            httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<Map<String,String>> entity= new HttpEntity<>(request,httpHeaders);
            String uri = chatbotUrl+"/check-file";
            ResponseEntity<ApiResponse<CheckFileResponse>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            CheckFileResponse result = response.getBody().getData();
            if((!result.isContainsSensitiveWords() && result.getSensitiveWords()==null) ||
                    (result.isContainsSensitiveWords() && result.getSensitiveWords().size()<10)){
                throw new Exception("File chứa từ nhạy cảm quá nhiều!");
            }
            document.setIsCheck(true);
            documentsRepository.save(document);

        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Document error: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }
}
