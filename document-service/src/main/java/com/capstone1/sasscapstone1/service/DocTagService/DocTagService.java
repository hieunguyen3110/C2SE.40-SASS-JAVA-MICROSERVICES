package com.capstone1.sasscapstone1.service.DocTagService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocTag;

import java.util.List;

public interface DocTagService {
    ApiResponse<DocTag> addTagToDocument(Long documentId, Long tagId);
    ApiResponse<String> removeTagFromDocument(Long documentId, Long tagId) throws Exception;
    ApiResponse<List<DocTag>> getTagsByDocument(Long documentId);
}
