package com.capstone1.sasscapstone1.controller.AdminController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.service.AuthenticationService.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/account")
public class AccountController {
    private final AuthenticationService authenticationService;

    @PutMapping("/active")
    public ApiResponse<String> handleAllowActiveAccount(@RequestParam("email") String email) throws Exception {
        return authenticationService.allowActiveAccount(email);
    }
}
