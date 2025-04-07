package com.capstone1.sasscapstone1.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentData {
    private Long document_id;
    private String title;
    private String category;
    private String content;
    private float popularity;
}
