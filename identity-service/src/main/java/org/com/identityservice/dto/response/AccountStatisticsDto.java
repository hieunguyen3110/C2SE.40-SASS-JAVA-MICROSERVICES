package org.com.identityservice.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStatisticsDto {
    private Long totalFollowers;
    private Long totalFollowing;
    private Long totalUploadedDocuments;
    public AccountStatisticsDto(Long totalFollowers, Long totalFollowing, Long totalUploadedDocuments) {
        this.totalFollowers = totalFollowers;
        this.totalFollowing = totalFollowing;
        this.totalUploadedDocuments = totalUploadedDocuments;
    }
}
