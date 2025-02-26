package com.capstone1.sasscapstone1.service.DocTagService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocTag;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Tags;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Tags.TagsRepository;
import com.capstone1.sasscapstone1.repository.DocTag.DocTagRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocTagServiceImpl implements DocTagService {

    @Autowired
    private DocTagRepository docTagRepository;

    @Autowired
    private DocumentsRepository documentsRepository;

    @Autowired
    private TagsRepository tagRepository;

    @Override
    public ApiResponse<DocTag> addTagToDocument(Long documentId, Long tagId) {
        // Tìm document theo ID
        Documents document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document not found"));

        // Tìm tag theo ID
        Tags tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Tag not found"));

        // Tạo DocTag và lưu vào cơ sở dữ liệu
        DocTag docTag = new DocTag();
        docTag.setDocuments(document);
        docTag.setTags(tag);
        DocTag savedDocTag = docTagRepository.save(docTag);

        // Trả về ResponseEntity với mã trạng thái HTTP 201 CREATED
        return CreateApiResponse.createResponse(savedDocTag,true);
    }

    @Override
    public ApiResponse<String> removeTagFromDocument(Long documentId, Long tagId) throws Exception {
        // Tìm mối quan hệ giữa document và tag
        try{
            DocTag docTag = docTagRepository.findByDocuments_DocIdAndTags_TagId(documentId, tagId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document-Tag relation not found"));

            // Xóa mối quan hệ khỏi cơ sở dữ liệu
            docTagRepository.delete(docTag);

            // Trả về ResponseEntity với mã trạng thái HTTP 200 OK
            return CreateApiResponse.createResponse("Tag removed from document successfully",false);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<DocTag>> getTagsByDocument(Long documentId) {
        // Tìm tất cả các tag liên quan đến document
        List<DocTag> docTags = docTagRepository.findByDocuments_DocId(documentId);

        // Trả về ResponseEntity với danh sách tag
        return CreateApiResponse.createResponse(docTags,false);
    }
}
