package org.com.identityservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsDto {
    private long totalStudents;
    private long totalLecturers;
    private long totalDocuments;
    private long totalFolders;
    private long totalSubjects;
    private long totalAdmins;
}

