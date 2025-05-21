package com.capstone1.sasscapstone1.dto.DocumentDetailDto;

import com.capstone1.sasscapstone1.dto.AccountRatingDto.AccountRatingDto;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long subjectId;
    private String subjectName;
    private String facultyName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private String authorName;
    private String filePath;
    private Float fileSize;
    private String profilePicture;
    private List<AccountRatingDto> accountRatingDtos;
}
