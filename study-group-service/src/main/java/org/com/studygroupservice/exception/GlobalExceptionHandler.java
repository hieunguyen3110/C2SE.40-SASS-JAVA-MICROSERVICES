package org.com.studygroupservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.response.ErrorResponse;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getCode(),exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorResponse.getCode()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleUnWantedException(Exception e){
        log.error(e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorResponse.getCode()));
    }
}