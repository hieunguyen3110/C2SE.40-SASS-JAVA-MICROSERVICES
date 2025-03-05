package org.com.identityservice.controller.Admin;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminAccountController")
@RequiredArgsConstructor
@RequestMapping("/admin/account")
public class AccountController {
    private final AccountService accountService;

    @PutMapping("/active")
    public ApiResponse<String> handleAllowActiveAccount(@RequestParam("email") String email) throws Exception {
        return accountService.allowActiveAccount(email);
    }

    // List users
    @GetMapping("/users")
    public ApiResponse<Page<AccountDto>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return CreateApiResponse.createResponse(accountService.listUsers(page, size),false);
    }

    // User details
    @GetMapping("/users/{accountId}")
    public ApiResponse<AccountDto> getUserDetails(@PathVariable Long accountId) {
        return CreateApiResponse.createResponse(accountService.getUserDetails(accountId),false);
    }

    // Delete users
    @DeleteMapping("/delete-users")
    public ApiResponse<String> softDeleteUsers(@RequestBody List<Long> accountIds) {
        try {
            accountService.softDeleteAccounts(accountIds);
            return CreateApiResponse.createResponse("Accounts successfully soft-deleted.",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    // Approve users
    @PostMapping("/users/approve")
    public ApiResponse<String> approveNewUsers(@RequestBody List<Long> accountIds) {
        return accountService.approveNewUsers(accountIds);
    }

    @DeleteMapping("/delete-profile-picture")
    public ApiResponse<String> adminDeleteUserProfilePicture(@RequestParam Long accountId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                return accountService.adminDeleteUserProfilePicture(accountId);
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
            }
        } else {
            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }
}
