package com.capstone1.sasscapstone1.service.SubjectService;


import com.capstone1.sasscapstone1.dto.SubjectDto.SubjectDto;
import com.capstone1.sasscapstone1.repository.Subject.SubjectRepository;
import com.capstone1.sasscapstone1.service.RedisService.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SUBJECT_KEY = "subject";
    private static final long TTL_IN_SECONDS = 30 * 24 * 60 * 60;


    @Override
    public List<SubjectDto> getAllSubjects() {
        try {
            List<SubjectDto> subjects = fetchSubjectsFromDatabase();
            cacheSubjectsInRedis(subjects);
            return subjects;
        } catch (Exception e) {
            log.error("Failed to fetch subjects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch subjects: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 30L * 24 * 60 * 60 * 1000)
    public void refreshSubjectsInRedis() {
        try {
            List<SubjectDto> subjects = fetchSubjectsFromDatabase();
            cacheSubjectsInRedis(subjects);
            log.info("Refreshed subjects in Redis successfully");
        } catch (Exception e) {
            log.error("Failed to refresh subjects in Redis: {}", e.getMessage(), e);
        }
    }

    private List<SubjectDto> fetchSubjectsFromDatabase() {
        return subjectRepository.findAll().stream()
                .map(subject -> {
                    SubjectDto dto = new SubjectDto();
                    dto.setSubjectId(subject.getSubjectId());
                    dto.setSubjectName(subject.getSubjectName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void cacheSubjectsInRedis(List<SubjectDto> subjects) {
        try {
            String jsonSubjects = objectMapper.writeValueAsString(subjects);
            redisService.saveData(SUBJECT_KEY, jsonSubjects, TTL_IN_SECONDS);
            log.info("Cached {} subjects in Redis under key: {} with TTL {} seconds (30 days)", subjects.size(), SUBJECT_KEY, TTL_IN_SECONDS);
        } catch (Exception e) {
            log.error("Failed to cache subjects in Redis: {}", e.getMessage(), e);
        }
    }

}
