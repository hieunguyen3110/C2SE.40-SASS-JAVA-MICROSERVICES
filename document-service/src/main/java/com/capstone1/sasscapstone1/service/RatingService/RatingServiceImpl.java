package com.capstone1.sasscapstone1.service.RatingService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.AccountRatingDto.AccountRatingDto;
import com.capstone1.sasscapstone1.dto.RatingDto.RatingDto;
import com.capstone1.sasscapstone1.dto.response.UserInteractionData;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Ratings;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Ratings.RatingsRepository;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.capstone1.sasscapstone1.request.RatingRequest;
import com.capstone1.sasscapstone1.service.RedisService.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService{
    private final RatingsRepository ratingsRepository;
    private final DocumentsRepository documentsRepository;
    private final IdentityClient identityClientWithoutSecurity;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private List<Ratings> ratings = null;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private RatingDto mapToDto(Ratings ratings){
        return RatingDto.builder()
                .rateId(ratings.getRateId())
                .docId(ratings.getDocuments().getDocId())
                .rating(ratings.getRating())
                .viewTime(ratings.getViewTime())
                .content(ratings.getContent())
                .isChecked(ratings.getIsChecked())
                .build();
    }
    private boolean checkIsRating(Long accountId, String docTitle, Long docId) throws JsonProcessingException {
        String key= docTitle+"_"+docId;
        String json= (String) redisService.getData(key);
        if(json!=null){
            List<AccountRatingDto> accountRatingDtos= objectMapper.readValue(json,new TypeReference<>() {});
            return accountRatingDtos.stream().anyMatch(accountRatingDto -> accountRatingDto.getAccountId()==accountId);
        }else{
            Optional<Ratings> isRating= ratingsRepository.findRatingsByAccountIdAndDocuments_DocId(accountId,docId);
            return isRating.isPresent();
        }
    }
    @Override
    public String rateDocumentByUser(AccountDto accountDto, RatingRequest request) throws Exception {
        try{
            Documents findDoc= documentsRepository.findByDocIdAndIsCheckTrueAndIsActiveTrue(request.getDocId())
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document is not found!!"));
            boolean checkedIsRated= checkIsRating(accountDto.getAccountId(),findDoc.getTitle(),findDoc.getDocId());
            if(checkedIsRated){
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User was rated to this document");
            }
            Ratings ratingsSaved= Ratings.builder()
                    .rating(request.getRating())
                    .documents(findDoc)
                    .accountId(accountDto.getAccountId())
                    .viewTime(request.getViewTime())
                    .content(request.getContent())
                    .isChecked(false)
                    .build();
            ratingsRepository.save(ratingsSaved);
            return "Document is rate successful";
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<RatingDto> getAllRatingByDayCheckedIsFalse() throws Exception {
        try{
            endDate= LocalDateTime.now();
            startDate= endDate.minusDays(1);
            ratings= ratingsRepository.findAllByCreatedAtAndIsChecked(startDate,endDate);
            return ratings.stream().map(this::mapToDto).toList();
        }catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<UserInteractionData> getAllRatingByDayCheckedIsTrue() throws Exception {
        try{
            ratings= ratingsRepository.findAllByCreatedAtAndIsCheckedIsTrue(startDate,endDate);
            return ratings.stream().map(rating->UserInteractionData.builder()
                            .document_id(rating.getDocuments().getDocId())
                            .account_id(rating.getAccountId())
                            .timestamp(rating.getCreatedAt())
                            .view_time(rating.getViewTime())
                            .rating(rating.getRating())
                    .build())
                    .toList();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public String saveRatingIsChecked(List<Long> ratingIds) throws Exception {
        try{
            List<Ratings> ratingsFilter= ratings.stream()
                    .filter(rating->ratingIds.contains(rating.getRateId()))
                    .toList();
            ratingsFilter.forEach(rating->rating.setIsChecked(true));
            List<Ratings> ratingsUpdated=ratingsRepository.saveAll(ratingsFilter);
            Map<Long, List<Ratings>> map= ratingsUpdated.stream()
                    .collect(Collectors.groupingBy(rating->rating.getDocuments().getDocId()));
            for(Map.Entry<Long, List<Ratings>> entry : map.entrySet()){
                Long docId= entry.getKey();
                Documents document= documentsRepository.findById(docId)
                        .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document is not found"));
                List<Ratings> ratingForDocs= entry.getValue();
                List<Long> accountIds= ratingForDocs.stream().map(Ratings::getAccountId).toList();
                String title= document.getTitle();
                String accountRatingDtos= identityClientWithoutSecurity.updateAccountRatedInRedis(accountIds,title,docId).getData();
                if(accountRatingDtos==null){
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Update account rating checked is fail");
                }
            }
            return "Update rating checked is successful";
        }catch (Exception e){
            log.error("Exception: "+e.getMessage());
            throw new Exception("Exception: "+ e.getMessage());
        }
    }
}
