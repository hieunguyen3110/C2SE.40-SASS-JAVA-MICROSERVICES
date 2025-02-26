package com.capstone1.sasscapstone1.service.ChatbotService;

import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.request.SendMessageRequest;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{
    private final RestTemplate restTemplate;
    private final DocumentsRepository documentsRepository;

    @Override
    public ApiResponse<ChatbotResponse> sendMessage(SendMessageRequest request) throws Exception {
        try{
            HttpHeaders httpHeaders= new HttpHeaders();
            httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<SendMessageRequest> entity= new HttpEntity<>(request,httpHeaders);
            String uri = "http://127.0.0.1:5000/api/search";
            ChatbotResponse result= restTemplate.postForEntity(uri,entity,ChatbotResponse.class).getBody();
            assert result != null;
            if (!result.getParts().get(0).getFile_source().isEmpty()) {
                Map<String, Long> fileFrequency = result.getParts().get(0).getFile_source().stream()
                        .collect(Collectors.groupingBy(file -> file, Collectors.counting()));
                // Sắp xếp danh sách file theo tần suất giảm dần
                List<String> sortedFiles = fileFrequency.entrySet().stream()
                        .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                        .map(Map.Entry::getKey)
                        .toList();

                // Tìm file xuất hiện nhiều nhất
                String mostFrequentFile = sortedFiles.get(0);
                Optional<Documents> findDoc = documentsRepository.findByFileNameAndIsActiveIsTrue(mostFrequentFile);
                if (findDoc.isPresent()) {
                    Documents getDoc = findDoc.get();
                    result.setFileName(getDoc.getFileName());
                    result.setUserName(getDoc.getAccount().getFirstName() + " " + getDoc.getAccount().getLastName());
                    result.setFilePath(getDoc.getFilePath());
                    result.setSubjectName(getDoc.getSubject().getSubjectName());
                    result.setDocId(getDoc.getDocId());
                }
            }
            return CreateApiResponse.createResponse(result,false);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
