package org.com.identityservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.service.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatsController {
    private final AccountService accountService;
    @GetMapping("/type")
    public ApiResponse<Long> countStatsByRoleName(@RequestParam("roleName") String roleName) {
        return CreateApiResponse.createResponse(accountService.countStatsByRoleName(roleName),false);
    }
}
