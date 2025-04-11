package org.com.batchservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.request.CheckFileRequest;
import org.com.batchservice.dto.request.UploadFileRequest;
import org.com.batchservice.dto.response.ApiResponse;
import org.com.batchservice.dto.response.CheckFileResponse;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.repository.ChatbotClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckDocAndTrainDocProcessorCustom implements ItemProcessor<DocumentDto, DocumentDto> {
    private final ChatbotClient chatbotClient;
    @Override
    public DocumentDto process(@NotNull DocumentDto item) throws Exception {
        try{
            CheckFileResponse response= chatbotClient.checkFile(CheckFileRequest.builder()
                            .filePath(item.getFilePath())
                    .build()).getData();
            if((!response.isContainsSensitiveWords() && response.getSensitiveWords().isEmpty()) ||
                    (response.isContainsSensitiveWords() && response.getSensitiveWords().size()<10)
            ){
                int statusCode2= chatbotClient.uploadFile(UploadFileRequest.builder()
                                .docId(item.getDocId())
                                .fileName(item.getFileName())
                                .filePath(item.getFilePath())
                        .build())
                        .getCode();
                if(statusCode2==200){
                    return item;
                }else{
                    throw new Exception("Train ai is failed");
                }
            }else{
                throw new Exception("Check sensitive file is failed");
            }
        }catch (RuntimeException e){
            log.error("Runtime exception: "+ e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
