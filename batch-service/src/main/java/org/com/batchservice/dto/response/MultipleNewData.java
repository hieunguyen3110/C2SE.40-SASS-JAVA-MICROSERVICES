package org.com.batchservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipleNewData {
    private List<DocumentData> updateDocuments;
    private List<DocumentData> newDocuments;
    private List<UserInteractionData> userInteractions;
    private List<UserNewsData> userRequests;
}
