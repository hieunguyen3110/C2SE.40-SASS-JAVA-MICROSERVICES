package com.capstone1.sasscapstone1.service.DocumentSearchService;

import com.capstone1.sasscapstone1.dto.DocumentDetailDto.DocumentDetailDto;
import com.capstone1.sasscapstone1.dto.DocumentSearchDto.DocumentSearchDto;
import com.capstone1.sasscapstone1.dto.FacultyDto.FacultyDto;
import com.capstone1.sasscapstone1.dto.SubjectDto.SubjectDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DocumentSearchService {
    ApiResponse<List<DocumentSearchDto>> searchDocByTitle(String title);
    ApiResponse<List<SubjectDto>> searchBySubject(String subject);
    ApiResponse<List<FacultyDto>> searchByFacultyName(String facultyName);
    ApiResponse<List<DocumentSearchDto>> searchDocBySubject(String subjectName);
    ApiResponse<List<DocumentSearchDto>> searchDocByFolderName(String folderName);
    ApiResponse<List<DocumentSearchDto>> searchDocByFacultyName(String facultyName);
    ApiResponse<Page<DocumentDetailDto>> searchDocumentsInFolder(Long folderId, Long userId, String keyword, int page, int size);
}
