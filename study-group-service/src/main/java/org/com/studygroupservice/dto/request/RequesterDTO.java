package org.com.studygroupservice.dto.request;


import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class RequesterDTO {
    @NotNull
    private Long requesterId;
}
