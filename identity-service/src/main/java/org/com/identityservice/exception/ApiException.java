package org.com.identityservice.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})
public class ApiException extends RuntimeException{
    private int code;
    private String message;
}
