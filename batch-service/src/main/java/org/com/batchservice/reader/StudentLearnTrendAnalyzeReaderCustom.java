package org.com.batchservice.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.dto.response.AnalyzeData;
import org.com.batchservice.handler.SendNotificationProducer;
import org.com.batchservice.repository.ElearningClient;
import org.com.batchservice.repository.IdentityClient;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentLearnTrendAnalyzeReaderCustom implements ItemReader<AnalyzeData> {
    private final IdentityClient identityClient;
    private final ElearningClient elearningClient;
    private final SendNotificationProducer sendNotificationProducer;
    private List<AnalyzeData> analyzeData;
    private boolean readDone = false;
    private int currentIndex=0;
    @Override
    public AnalyzeData read() throws Exception {
        if (readDone) return null;

        try {
            if (analyzeData == null) {
                List<AccountDto> accountNeedAnalyze = identityClient.getAllAccountNeedAnalyze().getData();
                if (accountNeedAnalyze == null || accountNeedAnalyze.isEmpty()) {
                    log.info("No accounts to analyze.");
                    readDone = true;
                    return null;
                }

                List<Long> accountIds = accountNeedAnalyze.stream()
                        .map(AccountDto::getAccountId)
                        .toList();
                log.info("Total accounts to analyze: {}", accountIds.size());

                analyzeData = elearningClient.getDataAnalyzeByAccountIds(accountIds).getData();

                Set<Long> accountIdsHashData = analyzeData.stream()
                        .map(AnalyzeData::getAccountId)
                        .collect(Collectors.toSet());

                List<Long> filterAccountNotData = accountIds.stream()
                        .filter(id -> !accountIdsHashData.contains(id))
                        .toList();

                sendNotificationProducer.sendNotificationToAccount(filterAccountNotData);
            }

            if (analyzeData != null && currentIndex < analyzeData.size()) {
                return analyzeData.get(currentIndex++);
            } else {
                readDone = true;
                return null;
            }

        } catch (Exception e) {
            log.error("Error while reading data for student analysis", e);
            throw new Exception("Failed to read data in StudentLearnTrendAnalyzeReader", e);
        }
    }


}
