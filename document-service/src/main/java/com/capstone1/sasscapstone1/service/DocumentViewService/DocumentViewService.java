package com.capstone1.sasscapstone1.service.DocumentViewService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.request.ViewTimeRequest;

public interface DocumentViewService {
    String saveViewLog(AccountDto accountDto, ViewTimeRequest request) throws Exception;
}
