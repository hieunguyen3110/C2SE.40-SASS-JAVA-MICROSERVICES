package org.com.identityservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.com.identityservice.dto.response.ErrorResponse;
import org.com.identityservice.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception){
        log.error(exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(exception.getCode(),exception.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getCode())).body(errorResponse);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException exception){
        log.error(exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST.getStatusCode().value(),exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorResponse.getCode()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleUnWantedException(Exception e){
        log.error(e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"INTERNAL SERVER ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorResponse.getCode()));
    }
}
