package org.com.studygroupservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long memberId;
    private String name;
    private String email;
    private String profilePicture;
    private String role;
}
