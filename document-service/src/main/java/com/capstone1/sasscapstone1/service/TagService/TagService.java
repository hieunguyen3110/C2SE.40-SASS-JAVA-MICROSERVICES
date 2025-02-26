package com.capstone1.sasscapstone1.service.TagService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Tags;

import java.util.List;

public interface TagService {
    ApiResponse<Tags> createTag(String tagName) throws Exception;
    ApiResponse<Tags>  updateTag(Long tagId, String tagName);
    void deleteTag(Long tagId);
    List<Tags> getAllTags();
}
