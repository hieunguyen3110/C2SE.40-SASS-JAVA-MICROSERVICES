package org.com.identityservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.UpdateMessageAnalyzeRequest;
import org.com.identityservice.entity.Account;
import org.com.identityservice.entity.Analyze;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.repository.AccountRepository;
import org.com.identityservice.repository.AnalyzeRepository;
import org.com.identityservice.service.AnalyzeService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyzeServiceImpl implements AnalyzeService {
    private final AnalyzeRepository analyzeRepository;
    private final AccountRepository accountRepository;
    @Override
    public String updateAnalyzeMessage(UpdateMessageAnalyzeRequest request) throws Exception {
        try{
            Account existAccount= accountRepository.findByAccountId(request.getAccountId())
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account is not found"));
            Analyze analyze;
            if(existAccount.getAnalyzes().isEmpty()){
                analyze= Analyze.builder()
                        .account(existAccount)
                        .message(request.getMessage())
                        .build();
            }else{
                analyze= existAccount.getAnalyzes().get(0);
                analyze.setMessage(request.getMessage());
            }
            analyzeRepository.save(analyze);
            return "update analyze message is successful";
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
