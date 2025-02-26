package com.capstone1.sasscapstone1.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    BAD_REQUEST("Bad Request", HttpStatus.BAD_REQUEST),
    BAD_GATEWAY("Bad gateway", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("Not Found", HttpStatus.NOT_FOUND),
    CONFLICT("Conflict", HttpStatus.CONFLICT),
    UNPROCESSABLE_ENTITY("Unprocessable Entity", HttpStatus.UNPROCESSABLE_ENTITY),
    INTERNAL_SERVER_ERROR("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_IMPLEMENTED("Not Implemented", HttpStatus.NOT_IMPLEMENTED),
    SERVICE_UNAVAILABLE("Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    ErrorCode(String message, HttpStatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    private final String message;
    private final HttpStatusCode statusCode;
}
