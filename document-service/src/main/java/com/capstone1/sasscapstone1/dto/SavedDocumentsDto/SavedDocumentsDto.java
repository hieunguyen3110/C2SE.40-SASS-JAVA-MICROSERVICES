package com.capstone1.sasscapstone1.dto.SavedDocumentsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedDocumentsDto {
    private Long savedId;
    private Long docId;
    private String title;
    private String description;
    private String filePath;
}
