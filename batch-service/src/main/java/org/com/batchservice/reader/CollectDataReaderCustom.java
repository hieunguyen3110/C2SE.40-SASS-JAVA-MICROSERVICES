package org.com.batchservice.reader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.dto.response.MultipleNewData;
import org.com.batchservice.dto.response.UserNewsData;
import org.com.batchservice.repository.DocumentClient;
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
@Setter
@Getter
public class CollectDataReaderCustom implements ItemReader<MultipleNewData> {
    private final DocumentClient documentClient;
    private final IdentityClient identityClient;
    private boolean readDone = false;
    @Override
    public MultipleNewData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (readDone) return null; // Dừng sau 1 lần đọc

        try {
            MultipleNewData multipleNewData = documentClient.collectData().getData();
            List<AccountDto> accountDtos = identityClient.getAllNewAccountByDayActive().getData();
            List<UserNewsData> userNewsData = accountDtos.stream()
                    .map(account -> UserNewsData.builder().account_id(account.getAccountId()).build())
                    .toList();
            multipleNewData.setUserRequests(userNewsData);

            readDone = true; // Đánh dấu đã đọc xong
            return multipleNewData;
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
