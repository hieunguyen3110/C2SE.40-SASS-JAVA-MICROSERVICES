package org.com.elearningservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.request.EnableAnalyzeRequest;
import org.com.elearningservice.dto.response.AccountDto;
import org.com.elearningservice.dto.response.CoursePeriodDto;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.entity.CoursePeriod;
import org.com.elearningservice.repository.CoursePeriodRepository;
import org.com.elearningservice.service.CoursePeriodService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoursePeriodServiceImpl implements CoursePeriodService {
    private final CoursePeriodRepository coursePeriodRepository;


    @Override
    public CoursePeriodDto checkExistCoursePeriod(Long accountId) throws Exception {
        try{
            LocalDateTime now= LocalDateTime.now();
            Optional<CoursePeriod> getCourseExpired= coursePeriodRepository.findByAccountIdAndStartDateAndEndDate(accountId,now);
            if(getCourseExpired.isPresent()){
                CoursePeriod coursePeriod= getCourseExpired.get();
                return CoursePeriodDto.builder()
                        .coursePeriodId(coursePeriod.getCoursePeriodId())
                        .subjects(coursePeriod.getSubjects())
                        .build();
            }
            return null;
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Override
    public String saveCoursePeriod(EnableAnalyzeRequest request, AccountDto accountDto) throws Exception {
        try{
            LocalDateTime now= LocalDateTime.now();
            LocalDateTime endDate= now.plusDays(60);
            Map<Long, String> subjectMaps= new HashMap<>();
            for(SubjectDTO subjectDTO: request.getSubjects()){
                subjectMaps.put(subjectDTO.getSubjectId(), subjectDTO.getSubjectName());
            }
            CoursePeriod coursePeriod= CoursePeriod.builder()
                    .accountId(accountDto.getAccountId())
                    .startDate(now)
                    .endDate(endDate)
                    .subjects(subjectMaps)
                    .build();
            coursePeriodRepository.save(coursePeriod);
            return "Save course period successful";
        }catch (Exception e){
            throw new Exception(e);
        }
    }
}
