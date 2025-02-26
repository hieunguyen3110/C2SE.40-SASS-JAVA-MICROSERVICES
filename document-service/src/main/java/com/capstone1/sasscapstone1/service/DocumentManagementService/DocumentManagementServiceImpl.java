package com.capstone1.sasscapstone1.service.DocumentManagementService;

import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentListDto.DocumentListDto;
import com.capstone1.sasscapstone1.entity.*;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.repository.Subject.SubjectRepository;
import com.capstone1.sasscapstone1.service.KafkaService.KafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentManagementServiceImpl implements DocumentManagementService {

    private final DocumentsRepository documentsRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final FolderRepository folderRepository;
    private final KafkaService kafkaService;

    @Override
    public Page<DocumentListDto> listDocuments(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return documentsRepository.findActiveDocuments(pageable)
                    .map(this::mapToDocumentListDto);
        } catch (Exception e) {
            throw new RuntimeException("Error listing documents: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<DocumentListDto> searchDocuments(String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return documentsRepository.searchActiveDocuments(keyword, pageable)
                    .map(this::mapToDocumentListDto);
        } catch (Exception e) {
            throw new RuntimeException("Error searching documents: " + e.getMessage(), e);
        }
    }

    @Override
    public void softDeleteDocuments(List<Long> docIds) {
        try {
            List<Documents> documents = documentsRepository.findAllById(docIds);
            documents.forEach(doc -> doc.setIsDeleted(true));
            documentsRepository.saveAll(documents);
        } catch (Exception e) {
            throw new RuntimeException("Error performing soft delete: " + e.getMessage(), e);
        }
    }

    @Override
    public void approveDocuments(List<Long> docIds, String adminApprove) {
        for (Long docId : docIds) {
            try {
                // Lấy tài liệu từ database
                Documents document = documentsRepository.findByDocIdAndIsCheckTrue(docId)
                        .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document not found with ID: " + docId));

                // Cập nhật tài liệu với tên admin
                document.setApprovedBy(adminApprove);
                document.setIsActive(true);

                // Lưu tài liệu đã duyệt
                documentsRepository.save(document);
                kafkaService.sendNotificationFromUserFollower(document.getFileName(),document.getAccount());
            }catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred with document ID: " + docId + ". " + e.getMessage(), e);
            }
        }
    }


    @Override
    public AdminDocumentDto getDocumentDetails(Long docId) {
        try {
            Documents document = documentsRepository.findById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            AdminDocumentDto dto = new AdminDocumentDto();
            dto.setDocId(document.getDocId());
            dto.setTitle(document.getTitle());
            dto.setSubjectName(document.getSubject().getSubjectName());
            dto.setFacultyName(document.getFaculty().getFacultyName());
            dto.setFolderId(document.getFolder().getFolderId());
            dto.setDescription(document.getDescription());
            dto.setType(document.getType());
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching document details: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDocument(Long docId, AdminDocumentDto documentDto) {
        try {
            // Tìm tài liệu theo docId
            Documents document = documentsRepository.findById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Cập nhật các trường thông tin cơ bản
            document.setTitle(documentDto.getTitle());
            document.setDescription(documentDto.getDescription());
            document.setType(documentDto.getType());

            // Cập nhật môn học
            if (documentDto.getSubjectName() != null) {
                Subject subject = subjectRepository.findBySubjectName(documentDto.getSubjectName())
                        .orElse(null); // Trả về null nếu không tìm thấy
                document.setSubject(subject);
            }

            // Cập nhật khoa
            if (documentDto.getFacultyName() != null) {
                Faculty faculty = facultyRepository.findByFacultyName(documentDto.getFacultyName())
                        .orElse(null); // Trả về null nếu không tìm thấy
                document.setFaculty(faculty);
            }

            // Cập nhật thư mục theo folderId
            if (documentDto.getFolderId() != null) {
                Folder folder = folderRepository.findById(documentDto.getFolderId())
                        .orElseThrow(() -> new RuntimeException("Folder not found with ID: " + documentDto.getFolderId()));
                document.setFolder(folder);
            } else {
                document.setFolder(null); // Gán null nếu không có folderId
            }

            // Lưu lại tài liệu
            documentsRepository.save(document);
        } catch (Exception e) {
            throw new RuntimeException("Error updating document: " + e.getMessage(), e);
        }
    }

    private DocumentListDto mapToDocumentListDto(Documents document) {
        DocumentListDto dto = new DocumentListDto();
        dto.setDocId(document.getDocId());
        dto.setTitle(document.getTitle());
        dto.setFileName(document.getFileName());
        dto.setFilePath(document.getFilePath());
        dto.setSubjectName(document.getSubject() != null ? document.getSubject().getSubjectName() : "N/A");
        dto.setFolderName(document.getFolder() != null ? document.getFolder().getFolderName() : "N/A");
        dto.setAuthorName(document.getAccount() != null ? document.getAccount().getFirstName() + " " + document.getAccount().getLastName() : "N/A");
        dto.setCreatedAt(document.getCreatedAt() != null ? document.getCreatedAt().toString() : "N/A");
        dto.setApprovedBy(document.getApprovedBy());
        dto.setIsActive(document.getIsActive());
        dto.setIsCheck(document.getIsCheck());
        dto.setIsTrain(document.getIsTrain());
        return dto;
    }
}
