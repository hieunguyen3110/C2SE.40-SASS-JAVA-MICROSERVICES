package com.capstone1.sasscapstone1.dto.AccountStatisticsDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
