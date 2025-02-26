package com.capstone1.sasscapstone1.util;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class CreateApiResponse {
    public static <T> ApiResponse<T> createResponse(T data, boolean isCreated) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(isCreated?201:200);
        response.setMessage(isCreated?"created":"success");
        response.setData(data);
        return response;
    }
}
