package org.com.studygroupservice.service;

import org.com.studygroupservice.dto.response.SearchGroupResponse;
import org.com.studygroupservice.dto.response.SubjectDto;
import org.com.studygroupservice.entity.StudyGroup;

import java.util.List;

public interface SearchStudyGroupService {
    List<SearchGroupResponse> searchStudyGroup(String keyword);
    int getMemberCount(Long groupId);
}
