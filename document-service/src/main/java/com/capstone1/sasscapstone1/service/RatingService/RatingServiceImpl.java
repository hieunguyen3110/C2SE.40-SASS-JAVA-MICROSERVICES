package com.capstone1.sasscapstone1.service.RatingService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.AccountRatingDto.AccountRatingDto;
import com.capstone1.sasscapstone1.dto.RatingDto.RatingDto;
import com.capstone1.sasscapstone1.dto.response.DocumentData;
import com.capstone1.sasscapstone1.dto.response.UserInteractionData;
import com.capstone1.sasscapstone1.entity.DocumentView;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Ratings;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.DocumentView.DocumentViewRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService{
    private final RatingsRepository ratingsRepository;
    private final DocumentsRepository documentsRepository;
    private final IdentityClient identityClientWithoutSecurity;
    private final DocumentViewRepository documentViewRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final IdentityClient identityClient;

    private RatingDto mapToDto(Ratings ratings){
        return RatingDto.builder()
                .rateId(ratings.getRateId())
                .docId(ratings.getDocuments().getDocId())
                .rating(ratings.getRating())
                .build();
    }
    private Boolean checkIsRating(Long accountId, String docTitle, Long docId) throws JsonProcessingException {
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
            Boolean checkedIsRated= checkIsRating(accountDto.getAccountId(),findDoc.getTitle(),findDoc.getDocId());
            if(checkedIsRated){
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document is rated");
            }else{
                Ratings ratingsSaved= Ratings.builder()
                        .rating(request.getRating())
                        .documents(findDoc)
                        .accountId(accountDto.getAccountId())
                        .build();
                ratingsRepository.save(ratingsSaved);
                identityClient.updateAccountRatedInRedis(List.of(accountDto.getAccountId()),findDoc.getTitle(),findDoc.getDocId(),request.getRating());
                return "Document is rate successful";
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<RatingDto> getAllRatingByDayCheckedIsFalse() throws Exception {
        try{
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(2);
            List<Ratings> ratings = ratingsRepository.findAllByCreatedAtAndIsChecked(startDate, endDate);
            return ratings.stream().map(this::mapToDto).toList();
        }catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<UserInteractionData> getUserInteractionData() throws Exception {
        try{
            LocalDateTime endDate= LocalDateTime.now();
            LocalDateTime startDate= endDate.minusDays(1);
            List<UserInteractionData> userInteractionData= new ArrayList<>();
            List<DocumentView> documentViews= documentViewRepository.findAllByCreatedAtBetween(startDate,endDate);
            List<Ratings> allRatings= ratingsRepository.findAllByCreatedAt(startDate,endDate);
            Map<String, Ratings> ratingMap = allRatings.stream().collect(Collectors.toMap(
                    r -> r.getAccountId() + "_" + r.getDocuments().getDocId(),
                    r -> r
            ));
            if(documentViews.isEmpty()) return new ArrayList<>();

            Map<Long,List<DocumentView>> documentViewMap= documentViews.stream()
                    .collect(Collectors.groupingBy(DocumentView::getAccountId));
            for(Map.Entry<Long,List<DocumentView>> entry : documentViewMap.entrySet()){
                Long accountId= entry.getKey();
                Map<Long,List<DocumentView>> mapDocs= entry.getValue().stream()
                        .collect(Collectors.groupingBy(DocumentView::getDocumentId));
                for(Map.Entry<Long,List<DocumentView>> entrySet: mapDocs.entrySet()){
                    Long docId= entrySet.getKey();
                    Ratings existRating= ratingMap.get(accountId+"_"+docId);
                    double avgDuration = entrySet.getValue().stream()
                            .mapToLong(DocumentView::getDurationSeconds)
                            .average()
                            .orElse(0.0);
                    UserInteractionData userInteraction= UserInteractionData.builder()
                            .document_id(docId)
                            .account_id(accountId)
                            .timestamp(endDate)
                            .view_time(avgDuration)
                            .build();
                    if(existRating !=null){
                        userInteraction.setRating(existRating.getRating());
                    }else{
                        userInteraction.setRating(0);
                    }
                    userInteractionData.add(userInteraction);
                }
            }
            return userInteractionData;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

//    @Override
//    @Transactional
//    public String saveRatingIsChecked(List<Long> ratingIds) throws Exception {
//        try{
//            List<Ratings> ratingsFilter= ratings.stream()
//                    .filter(rating->ratingIds.contains(rating.getRateId()))
//                    .toList();
////            ratingsFilter.forEach(rating->rating.setIsChecked(true));
//            List<Ratings> ratingsUpdated=ratingsRepository.saveAll(ratingsFilter);
//            Map<Long, List<Ratings>> map= ratingsUpdated.stream()
//                    .collect(Collectors.groupingBy(rating->rating.getDocuments().getDocId()));
//            for(Map.Entry<Long, List<Ratings>> entry : map.entrySet()){
//                Long docId= entry.getKey();
//                Documents document= documentsRepository.findById(docId)
//                        .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document is not found"));
//                List<Ratings> ratingForDocs= entry.getValue();
//                List<Long> accountIds= ratingForDocs.stream().map(Ratings::getAccountId).toList();
//                String title= document.getTitle();
//                String accountRatingDtos= identityClientWithoutSecurity.updateAccountRatedInRedis(accountIds,title,docId).getData();
//                if(accountRatingDtos==null){
//                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Update account rating checked is fail");
//                }
//            }
//            return "Update rating checked is successful";
//        }catch (Exception e){
//            log.error("Exception: "+e.getMessage());
//            throw new Exception("Exception: "+ e.getMessage());
//        }
//    }
}
