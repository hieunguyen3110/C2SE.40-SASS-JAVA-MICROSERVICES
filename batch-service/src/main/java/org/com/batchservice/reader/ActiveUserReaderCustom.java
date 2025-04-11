package org.com.batchservice.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.repository.IdentityClient;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActiveUserReaderCustom implements ItemReader<AccountDto> {
    private final IdentityClient identityClient;
    private List<AccountDto> accountDtoList;
    private int currentIndex=0;
    @Override
    public AccountDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(accountDtoList==null){
            accountDtoList= identityClient.getAllNewAccountByDay().getData();
        }
        if(currentIndex<accountDtoList.size()){
            return accountDtoList.get(currentIndex++);
        }else{
            return null;
        }
    }
}
