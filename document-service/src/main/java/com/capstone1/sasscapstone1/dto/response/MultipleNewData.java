package com.capstone1.sasscapstone1.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipleNewData {
    private List<DocumentData> documents;
    private List<UserInteractionData> user_interactions;
    private List<UserNewsData> user_requests;
}
