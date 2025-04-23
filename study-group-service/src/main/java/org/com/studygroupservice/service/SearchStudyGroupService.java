package org.com.studygroupservice.service;

import org.com.studygroupservice.dto.response.SearchGroupResponse;
import org.com.studygroupservice.dto.response.SubjectDto;
import org.com.studygroupservice.entity.StudyGroup;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchStudyGroupService {
    @Query("SELECT g FROM StudyGroup g WHERE g.name LIKE %:keyword%")
    List<SearchGroupResponse> searchStudyGroup(String keyword);
    int getMemberCount(Long groupId);
}
