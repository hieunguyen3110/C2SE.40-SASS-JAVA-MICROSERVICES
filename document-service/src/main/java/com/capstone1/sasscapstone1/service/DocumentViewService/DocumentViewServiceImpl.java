package com.capstone1.sasscapstone1.service.DocumentViewService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.entity.DocumentView;
import com.capstone1.sasscapstone1.repository.DocumentView.DocumentViewRepository;
import com.capstone1.sasscapstone1.request.ViewTimeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentViewServiceImpl implements DocumentViewService{
    private final DocumentViewRepository documentViewRepository;
    @Override
    public void saveViewLog(AccountDto accountDto, ViewTimeRequest request) throws Exception {
        try{
            DocumentView documentView= DocumentView.builder()
                    .documentId(request.getDocId())
                    .accountId(accountDto.getAccountId())
                    .startViewTime(request.getStartTime())
                    .durationSeconds(request.getDuration())
                    .build();
            documentViewRepository.save(documentView);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
