package org.com.studygroupservice.controller;

import org.com.studygroupservice.dto.response.ApiResponse;
import org.com.studygroupservice.dto.response.SearchGroupResponse;
import org.com.studygroupservice.helpers.CreateApiResponse;
import org.com.studygroupservice.service.SearchStudyGroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search-group")
public class SearchStudyGroupController {

    private final SearchStudyGroupService searchStudyGroupService;

    public SearchStudyGroupController(SearchStudyGroupService searchStudyGroupService) {
        this.searchStudyGroupService = searchStudyGroupService;
    }

    @GetMapping("/search")
    public ApiResponse<List<SearchGroupResponse>> searchPublicGroups(@RequestParam String keyword) {
        List<SearchGroupResponse> response = searchStudyGroupService.searchStudyGroup(keyword);
        return CreateApiResponse.createResponse(response, false);
    }
}