package com.capstone1.sasscapstone1.service.ChatbotService;

import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotDto;
import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.httpClient.ChatbotClient;
import com.capstone1.sasscapstone1.request.SendMessageRequest;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{
    private final DocumentsRepository documentsRepository;
    private final ChatbotClient chatbotClient;

    @Override
    public ApiResponse<ChatbotDto> sendMessage(SendMessageRequest request) throws Exception {
        try{
            ChatbotResponse result = chatbotClient.sendMessage(request).getData();
            ChatbotDto chatbotDto = new ChatbotDto();
            if(result.getQuery()!=null ){
                String responseText = result.getQuery().getImproved_answer();
                if(result.getQuery().getReference_document() != null){
                    String referenceDocument = result.getQuery().getReference_document();
                    List<String> fileSource = result.getFile_source();
                    Optional<Documents> findDoc = documentsRepository.findByFileNameAndIsActiveIsTrue(referenceDocument);
                    chatbotDto.setResponseText(responseText);
                    if (findDoc.isPresent()) {
                        Documents getDoc = findDoc.get();
                        chatbotDto.setFile_source(fileSource);
                        chatbotDto.setFileName(getDoc.getFileName());
                        chatbotDto.setFilePath(getDoc.getFilePath());
                        chatbotDto.setSubjectName(getDoc.getSubject().getSubjectName());
                        chatbotDto.setDocId(getDoc.getDocId());
                    }
                }else{
                    chatbotDto.setResponseText(responseText);
                    chatbotDto.setFile_source(new ArrayList<>());
                }
            }
            return CreateApiResponse.createResponse(chatbotDto,false);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
