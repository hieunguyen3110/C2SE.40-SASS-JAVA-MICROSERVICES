package org.com.studygroupservice.service.impl;

import org.apache.commons.lang.StringUtils;
import org.com.studygroupservice.dto.response.SearchGroupResponse;
import org.com.studygroupservice.dto.response.SubjectDto;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.com.studygroupservice.service.SearchStudyGroupService;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchStudyGroupServiceImpl implements SearchStudyGroupService{

    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupService studyGroupService;

    public SearchStudyGroupServiceImpl(StudyGroupRepository studyGroupRepository, StudyGroupService studyGroupService) {
        this.studyGroupRepository = studyGroupRepository;
        this.studyGroupService = studyGroupService;
    }

    @Transactional
    @Override
    public List<SearchGroupResponse> searchStudyGroup(String keyword) {
        try {
            List<StudyGroup> groups;
            groups = studyGroupRepository.searchGroupByNameContainingIgnoreCase(keyword);


            // Lấy tất cả SubjectDto một lần
            List<SubjectDto> allSubjects = studyGroupService.fetchSubjects();
            Map<Long, String> subjectMap = allSubjects != null ? allSubjects.stream()
                    .collect(Collectors.toMap(SubjectDto::getSubjectId, SubjectDto::getSubjectName, (a, b) -> a))
                    : Collections.emptyMap();

            // Ánh xạ sang SearchGroupResponse
            return groups.stream().map(group -> {
                SearchGroupResponse groupResponse = new SearchGroupResponse();
                groupResponse.setGroupId(group.getId());
                groupResponse.setGroupName(group.getName());
                groupResponse.setDescription(group.getDescription());
                groupResponse.setMemberLimited(group.getMemberLimited());
                groupResponse.setPrivate(group.getIsPrivate());

                String subjectName = subjectMap.get(group.getSubjectId());
                if (subjectName == null || subjectName.isEmpty()) {
                    subjectName = group.getSubjectId() != null ? subjectMap.getOrDefault(group.getSubjectId(), "Unknown") : "Unknown";
                }
                groupResponse.setSubjectName(subjectName);

                groupResponse.setMemberCount(getMemberCount(group.getId()));
                groupResponse.setPicture(group.getPicture());
                List<Long> memberIds = group.getMembers() != null ?
                        group.getMembers().stream()
                                .map(GroupMember::getAccountId)
                                .collect(Collectors.toList()) :
                        Collections.emptyList();
                groupResponse.setMemberIds(memberIds);
                return groupResponse;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException(500, "Fail to search study group: " + e.getMessage());
        }
    }

    @Override
    public int getMemberCount(Long groupId) {
        try {
            return studyGroupRepository.getMemberCount(groupId);
        } catch (Exception e) {
            throw new ApiException(500, "Fail to get member count: " + e.getMessage());
        }
    }

}
