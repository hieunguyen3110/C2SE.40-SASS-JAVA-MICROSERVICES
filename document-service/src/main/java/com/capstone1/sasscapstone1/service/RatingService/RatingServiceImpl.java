package com.capstone1.sasscapstone1.service.RatingService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Ratings;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Ratings.RatingsRepository;
import com.capstone1.sasscapstone1.request.RatingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService{
    private final RatingsRepository ratingsRepository;
    private final DocumentsRepository documentsRepository;
    @Override
    public String rateDocumentByUser(AccountDto accountDto, RatingRequest request) throws Exception {
        try{
            Documents findDoc= documentsRepository.findByDocIdAndIsCheckTrueAndIsActiveTrue(request.getDocId())
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document is not found!!"));

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
}
