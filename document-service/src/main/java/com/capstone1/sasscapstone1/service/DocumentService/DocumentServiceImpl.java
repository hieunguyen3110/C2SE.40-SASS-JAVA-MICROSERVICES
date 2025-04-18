package com.capstone1.sasscapstone1.service.DocumentService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.AccountRatingDto.AccountRatingDto;
import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentDetailDto.DocumentDetailDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.PopularDocumentDto.PopularDocumentDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.dto.response.DocumentData;
import com.capstone1.sasscapstone1.entity.*;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.repository.History.HistoryRepository;
import com.capstone1.sasscapstone1.repository.Subject.SubjectRepository;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.capstone1.sasscapstone1.repository.httpClient.RecommendationClient;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import com.capstone1.sasscapstone1.service.FirebaseService.FirebaseService;
import com.capstone1.sasscapstone1.service.RedisService.RedisService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import com.capstone1.sasscapstone1.util.UpdateDocumentPopularity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentsRepository documentsRepository;
    private final FolderRepository folderRepository;
    private final FacultyRepository facultyRepository;
    private final FirebaseService firebaseService;
    private final SubjectRepository subjectRepository;
    private final HistoryRepository historyRepository;
    private final RestTemplate restTemplate;
    private final IdentityClient identityClient;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final UpdateDocumentPopularity updateDocumentPopularity;
    private final RecommendationClient recommendationClient;
    private List<Documents> documents;
    @Value("${chatbot.url}")
    private String chatbotUrl;

    @Override
    public ApiResponse<String> uploadDocument(MultipartFile file,
                                              String title,
                                              String description,
                                              String content,
                                              String type,
                                              String subjectCode,
                                              String facultyName,
                                              String folderId,
                                              AccountDto account) throws Exception {
        try {
            if (file == null || title == null || title.isBlank() || subjectCode == null ) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Missing required fields: file, title, subjectName, or facultyName.");
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName.isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Invalid file name.");
            }
            String extension= FilenameUtils.getExtension(originalFileName);
            if (!extension.equalsIgnoreCase("pdf")) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Only pdf files are accepted.");
            }
            String lowerCaseType = type.toLowerCase();
            if (!lowerCaseType.equals("trắc nghiệm") && !lowerCaseType.equals("tự luận")) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Invalid document type. Only 'trắc nghiệm' and 'tự luận' are accepted.");
            }
            Faculty faculty=null;
            if(facultyName!=null){
                faculty = facultyRepository.findByFacultyName(facultyName)
                        .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Faculty not found"));
            }
            Subject subject = subjectRepository.findBySubjectCode(subjectCode)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Subject not found"));


            CompletableFuture<String> fileUploadFuture = firebaseService.save(file, originalFileName);
            String filePath;
            try {
                filePath = fileUploadFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error uploading file to Firebase: " + e.getMessage());
            }

            Folder folder = null;
            if (folderId != null) {
                folder = folderRepository.findByFolderIdAndAccountId(Long.parseLong(folderId), account.getAccountId())
                        .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Folder not found or does not belong to this user."));
            }

            // Lưu tài liệu vào cơ sở dữ liệu
            Documents document = new Documents();
            document.setTitle(title);
            document.setDescription(description);
            document.setContent(content);
            document.setType(lowerCaseType);
            document.setFilePath(filePath);
            document.setFileSize((int) file.getSize());
            document.setFileName(originalFileName);
            document.setSubject(subject);
            if(faculty!=null) document.setFaculty(faculty);
            document.setAccountId(account.getAccountId());
            if (folder != null) document.setFolder(folder);
            document.setIsActive(false);
            documentsRepository.save(document);

            return CreateApiResponse.createResponse("Document uploaded successfully! ID: " + document.getDocId() + ", URL: " + filePath,true);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }


    @Override
    public Page<DocumentDto> getAllDocuments(Pageable pageable) {
        try {
            return documentsRepository.findAll(pageable).map(this::mapToDocumentDto);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all documents: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<List<DocumentDto>> getDocumentByFolderId(Long folderId, int pageNum, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            Page<Documents> documentsPage = documentsRepository.findByFolder_FolderIdOrderByCreatedAtDesc(folderId, pageable);
            List<DocumentDto> documentDtos = documentsPage.map(this::mapToDocumentDto).toList();
            return CreateApiResponse.createResponse(documentDtos,false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error fetching documents by folder ID: " + e.getMessage());
        }
    }

    @Override
    public DocumentDetailDto getDocumentById(Long docId) {
        try {
            Documents document = documentsRepository.findById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            Set<Ratings> ratings= document.getRatings();
            String key= document.getTitle()+"_"+document.getDocId();
            String json= (String) redisService.getData(key);
            DocumentDetailDto documentDetailDto= mapToDocumentDetailDto(document);
            List<AccountRatingDto> accountRatingDtos;
            if(json == null){
                accountRatingDtos = new ArrayList<>();
                for(Ratings rating : ratings){
                    accountRatingDtos.add(AccountRatingDto.builder()
                                    .accountId(rating.getAccountId())
                                    .rating(rating.getRating())
                            .build());
                }
                documentDetailDto.setAccountRatingDtos(accountRatingDtos);
            }else{
                accountRatingDtos= objectMapper.readValue(json,new TypeReference<>() {});
                documentDetailDto.setAccountRatingDtos(accountRatingDtos);
            }
            return documentDetailDto;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching document by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<String> trainDocument(TrainDocumentRequest request) {
        try {
            String uri = chatbotUrl+"/upload-file";
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<TrainDocumentRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            ApiResponse<String> result = response.getBody();
            if(result.getCode()==200){
                Optional<Documents> findDocByFilePath= documentsRepository.findByFilePath(request.getFilePath());
                if(findDocByFilePath.isPresent()){
                    Documents documents= findDocByFilePath.get();
                    documents.setIsTrain(true);
                    documentsRepository.save(documents);
                }
            }
            return result;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error during document training.");
        }
    }

    @Override
    public ApiResponse<List<DocumentDto>> findAllByAccount(AccountDto account, int pageNum, int pageSize) throws Exception {
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            Page<Documents> documentsPage = documentsRepository.findAllByAccountIdAndIsActiveIsTrue(account.getAccountId(), pageable);
            List<DocumentDto> documentDtos = documentsPage.map(this::mapToDocumentDto).toList();
            return CreateApiResponse.createResponse(documentDtos,false);
        } catch (Exception e) {
            throw new Exception("Error fetching documents by account: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<DocumentDto>> findAllByAccount(String email, int pageNum, int pageSize) throws Exception {
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            AccountDto accountDto= identityClient.getAccountEmail(email).getData();
            Page<Documents> documentsPage = documentsRepository.findAllByAccountIdAndIsActiveIsTrue(accountDto.getAccountId(), pageable);
            List<DocumentDto> documentDtos = documentsPage.map(this::mapToDocumentDto).toList();
            return CreateApiResponse.createResponse(documentDtos,false);
        } catch (Exception e) {
            throw new Exception("Error fetching documents by account: " + e.getMessage());
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

    private DocumentDto mapToDocumentDto(Documents document) {
        DocumentDto dto = new DocumentDto();
        dto.setDocId(document.getDocId());
        dto.setTitle(document.getTitle());
        dto.setDescription(document.getDescription());
        dto.setFilePath(document.getFilePath());
        return dto;
    }

    private DocumentDetailDto mapToDocumentDetailDto(Documents document) {
        DocumentDetailDto dto = new DocumentDetailDto();
        dto.setDocId(document.getDocId());
        dto.setTitle(document.getTitle());
        dto.setFilePath(document.getFilePath());
        dto.setFolderName(document.getFolder() != null ? document.getFolder().getFolderName() : null);
        dto.setSubjectName(document.getSubject().getSubjectName());
        dto.setFacultyName(document.getFaculty().getFacultyName());
        dto.setCreatedAt(document.getCreatedAt());
        AccountDto getAccount= identityClient.getAccountId(document.getAccountId()).getData();
        if(getAccount != null){
            dto.setAuthorName(getAccount.getFirstName() + " " + getAccount.getLastName());
            dto.setProfilePicture(getAccount.getProfilePicture());
        }
        return dto;
    }
    private DocumentData mapToDocumentData(Documents documents){
        return DocumentData.builder()
                .document_id(documents.getDocId())
                .popularity(documents.getPopularity())
                .title(documents.getTitle())
                .category(documents.getSubject().getSubjectName())
                .content(documents.getDescription())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularDocumentDto> getPopularDocuments(AccountDto accountDto) {
        try {
            List<String> data= recommendationClient.getDocumentByModel(accountDto.getAccountId()).getData();
            List<Long> dataParseLong= data.stream()
                    .map(docId->Long.parseLong(docId.substring(4)))
                    .toList();
            List<Documents> documentsList= documentsRepository.findAllByDocIdIn(dataParseLong);
            return documentsList.stream().map(document->{
                AccountDto account= identityClient.getAccountId(document.getAccountId()).getData();
                return PopularDocumentDto.builder()
                        .authorName(account.getLastName())
                        .filePath(document.getFilePath())
                        .title(document.getTitle())
                        .profilePicture(account.getProfilePicture())
                        .docId(document.getDocId())
                        .subject(document.getSubject().getSubjectName())
                        .description(document.getDescription())
                        .facultyName(document.getFaculty().getFacultyName())
                        .build();
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching popular documents: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentDto> getAllDocumentByDay() throws Exception {
        try{
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate= endDate.minusDays(2);
            documents= documentsRepository.findAllByCreatedAtAndIsCheckAndIsTrain(startDate,endDate,false,false);
            return documents.stream().map(document->DocumentDto.builder()
                            .docId(document.getDocId())
                            .filePath(document.getFilePath())
                            .description(document.getDescription())
                            .fileName(document.getFileName())
                            .build()
                    ).toList();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateFileStatus(List<Long> docIds) throws Exception {
        try{
            List<Documents> documentsFilter= documents.stream()
                    .filter(document->docIds.contains(document.getDocId()))
                    .toList();
            documentsFilter.forEach(document->{
                document.setIsCheck(true);
                document.setIsTrain(true);
                document.setIsActive(true);
            });
            documentsRepository.saveAll(documentsFilter);
            return "Update file status is successful";
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, List<DocumentData>> collectNewData() {
        LocalDateTime now= LocalDateTime.now();
        LocalDateTime startDate= now.minusDays(1);
        List<Documents> updateDocPopularity= updateDocumentPopularity.updateDocumentPopularity(startDate,now);
        List<Documents> newlyCreateDoc= new ArrayList<>();
        List<Documents> docUpdated= new ArrayList<>();
        for(Documents document : updateDocPopularity){
            if(document.getCreatedAt().isAfter(startDate) && document.getCreatedAt().isBefore(now)){
                newlyCreateDoc.add(document);
            }else{
                docUpdated.add(document);
            }
        }
        List<DocumentData> newlyCreateDocData= newlyCreateDoc.stream()
                .map(this::mapToDocumentData)
                .toList();
        List<DocumentData> docUpdatedData= docUpdated.stream()
                .map(this::mapToDocumentData)
                .toList();
        Map<String, List<DocumentData>> map= new HashMap<>();
        map.put("documentUpdate",docUpdatedData);
        map.put("documentsNew",newlyCreateDocData);
        return map;
    }
}
