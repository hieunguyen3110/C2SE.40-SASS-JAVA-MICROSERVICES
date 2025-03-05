package org.com.identityservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long docId;
    private String title;
    private String description;
    private String type;
    private Boolean isActive;
    private String subjectName;
    private String filePath;
    private Long accountId;
    private Long facultyId;
    private String facultyName;
    private String subjectCode;
    private Long folderId;
    private String authorName;
    private String authorProfilePicture;
    private Long downloadCount;
}
