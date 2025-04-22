package org.com.studygroupservice.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JoinRequestDto {
    private Long id;
    private Long userId;
    private String status;
    private String avatar;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
