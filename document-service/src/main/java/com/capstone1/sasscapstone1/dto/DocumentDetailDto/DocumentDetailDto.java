package com.capstone1.sasscapstone1.dto.DocumentDetailDto;

import com.capstone1.sasscapstone1.dto.AccountRatingDto.AccountRatingDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDetailDto {
    private Long docId;
    private String title;
    private String folderName;
    private String subjectName;
    private String facultyName;
    private LocalDateTime createdAt;
    private String authorName;
    private String filePath;
    private String profilePicture;
    private List<AccountRatingDto> accountRatingDtos;
}
