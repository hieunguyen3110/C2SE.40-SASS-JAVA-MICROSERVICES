package com.capstone1.sasscapstone1.service.FolderService;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Folder;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.repository.History.HistoryRepository;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DocumentsRepository documentsRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Override
    @Transactional
    public FolderDto createFolder(String folderName, String description, Account account) {
        try {
            Account managedAccount = entityManager.merge(account);
            if (managedAccount == null) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account not found");
            }
            Optional<?> findFolderExistOfAccount = folderRepository.findByFolderNameAndAccountId(folderName, account.getAccountId());
            if (findFolderExistOfAccount.isPresent()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Folder name is exist");
            }

            Folder folder = new Folder();
            folder.setFolderName(folderName);
            folder.setDescription(description);
            folder.setAccount(managedAccount);

            folderRepository.save(folder);
            return mapToDTO(folder);
        } catch (Exception e) {
            throw new RuntimeException("Error creating folder: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FolderDto updateFolder(Long folderId, String folderName, String description) {
        try {
            Folder folder = entityManager.find(Folder.class, folderId);
            if (folder == null) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Folder not found with id " + folderId);
            }

            folder.setFolderName(folderName);
            folder.setDescription(description);
            folder.setUpdatedAt(java.time.LocalDateTime.now());

            entityManager.merge(folder);
            return mapToDTO(folder);
        } catch (Exception e) {
            throw new RuntimeException("Error updating folder: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        try {
            // Lấy tất cả tài liệu của folder
            List<Documents> documents = documentsRepository.findByFolder_FolderId(folderId);

            // Xóa tất cả tài liệu liên quan đến folder này
            documentsRepository.deleteAll(documents);

            // Sau đó xóa folder
            Optional<Folder> optionalFolder = folderRepository.findById(folderId);
            if (optionalFolder.isPresent()) {
                folderRepository.delete(optionalFolder.get());
            } else {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Folder not found with id " + folderId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting folder: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderDto> getAllFolders(Account account) {
        try {
            List<Folder> folders = folderRepository.findByAccount_AccountId(account.getAccountId());

            return folders.stream().map(folder -> {
                FolderDto dto = new FolderDto();
                dto.setFolderId(folder.getFolderId());
                dto.setFolderName(folder.getFolderName());
                dto.setDescription(folder.getDescription());
                dto.setOwnerName(folder.getAccount().getFirstName() + " " + folder.getAccount().getLastName());

                // Đếm số tài liệu trong thư mục
                long documentCount = documentsRepository.countByFolder_FolderIdAndIsActiveIsTrue(folder.getFolderId());
                dto.setDocumentCount(documentCount);

                // Thêm mã môn
                if (folder.getSubject() != null) {
                    dto.setSubjectCode(folder.getSubject().getSubjectCode());
                }

                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching folders: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<FolderResponse> getFolderById(Long accountId, Long folderId) {
        try {
            Optional<Folder> findFolderById = folderRepository.findByFolderIdAndAccountId(folderId, accountId);
            if (findFolderById.isPresent()) {
                Folder getFolder = findFolderById.get();
                Pageable pageable = PageRequest.of(0, 10);
                Page<Documents> documents = documentsRepository.findByFolder_FolderIdOrderByCreatedAtDesc(getFolder.getFolderId(), pageable);
                List<DocumentDto> documentDtos = new ArrayList<>();
                for (Documents document : documents) {
                    DocumentDto dto = DocumentDto.builder()
                            .docId(document.getDocId())
                            .title(document.getTitle())
                            .filePath(document.getFilePath())
                            .build();
                    documentDtos.add(dto);
                }
                return CreateApiResponse.createResponse((FolderResponse.builder()
                        .authorName(getFolder.getAccount().getFirstName() + " " + getFolder.getAccount().getLastName())
                        .numberDoc(documents.getTotalElements())
                        .documents(documentDtos)
                        .folderId(getFolder.getFolderId())
                        .folderName(getFolder.getFolderName())
                        .description(getFolder.getDescription())
                        .build()),false);
            } else {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Folder not exist");
            }
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error fetching folder by ID: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<FolderResponse> getFolderById(Long folderId) {
        try {
            Optional<Folder> findFolderById = folderRepository.findById(folderId);
            if (findFolderById.isPresent()) {
                Folder getFolder = findFolderById.get();
                Pageable pageable = PageRequest.of(0, 10);
                Page<Documents> documents = documentsRepository.findByFolder_FolderIdOrderByCreatedAtDesc(getFolder.getFolderId(), pageable);
                List<DocumentDto> documentDtos = new ArrayList<>();
                for (Documents document : documents) {
                    DocumentDto dto = DocumentDto.builder()
                            .docId(document.getDocId())
                            .title(document.getTitle())
                            .filePath(document.getFilePath())
                            .facultyName(document.getFaculty().getFacultyName())
                            .facultyId(document.getFaculty().getFacultyId())
                            .subjectCode(document.getSubject().getSubjectCode())
                            .description(document.getDescription())
                            .type(document.getType())
                            .subjectName(document.getSubject().getSubjectName())
                            .build();
                    documentDtos.add(dto);
                }
                return CreateApiResponse.createResponse((FolderResponse.builder()
                        .accountId(getFolder.getAccount().getAccountId())
                        .authorName(getFolder.getAccount().getFirstName() + " " + getFolder.getAccount().getLastName())
                        .numberDoc(documents.getTotalElements())
                        .documents(documentDtos)
                        .folderId(getFolder.getFolderId())
                        .folderName(getFolder.getFolderName())
                        .description(getFolder.getDescription())
                        .build()),false);
            } else {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Folder not exist");
            }
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error fetching folder by ID: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Page<FolderDownloadStatsDto> getTopFoldersByDownloadCount(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return historyRepository.getTopFoldersByDownloadCount(pageable);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching top folders by download count: " + e.getMessage(), e);
        }
    }

    private FolderDto mapToDTO(Folder folder) {
        FolderDto dto = new FolderDto();
        dto.setFolderId(folder.getFolderId());
        dto.setFolderName(folder.getFolderName());
        dto.setDescription(folder.getDescription());
        dto.setAccountEmail(folder.getAccount().getEmail());
        return dto;
    }
}
