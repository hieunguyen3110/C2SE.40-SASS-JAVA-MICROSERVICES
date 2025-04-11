package org.com.batchservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.repository.IdentityClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActiveUserProcessorCustom implements ItemProcessor<AccountDto,AccountDto> {
    private final IdentityClient identityClient;
    @Override
    public AccountDto process(AccountDto item) throws Exception {
        try{
            int statusCode= identityClient.updateAccountStatus(item.getAccountId()).getCode();
            if(statusCode==200){
                return item;
            }else{
                throw new Exception("Have error when call api to identity service");
            }
        }catch (Exception e){
            log.error("Exception when i approve account id: "+ item.getAccountId());
            throw new Exception(e.getMessage());
        }
    }
}
