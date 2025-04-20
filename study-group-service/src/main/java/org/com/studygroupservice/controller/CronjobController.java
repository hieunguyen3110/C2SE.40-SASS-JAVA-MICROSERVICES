package org.com.studygroupservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.dto.response.ApiResponse;
import org.com.studygroupservice.helpers.CreateApiResponse;
import org.com.studygroupservice.service.GroupMemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cronjob")
public class CronjobController {
    private final GroupMemberService groupMemberService;

    @GetMapping("/study-group/check-participate-group")
    public ApiResponse<Boolean> checkIsParticipateStudyGroup(@RequestParam("accountId") Long accountId) throws Exception {
        return CreateApiResponse.createResponse(groupMemberService.checkAccountParticipateGroup(accountId),false);
    }
}
