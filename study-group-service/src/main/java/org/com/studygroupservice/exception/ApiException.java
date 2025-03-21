package org.com.studygroupservice.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})
public class ApiException extends RuntimeException{
    private int code;
    private String message;
}
