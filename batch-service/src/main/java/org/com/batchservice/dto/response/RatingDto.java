package org.com.batchservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {
    private long rateId;
    private int rating;
    private String content;
    private float viewTime;
    private Boolean isChecked;
    private long accountId;
    private long docId;
}
