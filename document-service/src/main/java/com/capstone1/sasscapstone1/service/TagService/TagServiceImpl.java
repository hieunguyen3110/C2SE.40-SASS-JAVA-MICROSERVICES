package com.capstone1.sasscapstone1.service.TagService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Tags;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Tags.TagsRepository;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagsRepository tagRepository;

    @Override
    public ApiResponse<Tags> createTag(String tagName) throws Exception {
        try {
            Tags tag = new Tags();
            tag.setTagName(tagName);
            tagRepository.save(tag);
            return CreateApiResponse.createResponse(tag,true);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public ApiResponse<Tags> updateTag(Long tagId, String tagName) {
        Optional<Tags> tagOptional = tagRepository.findById(tagId);
        if (tagOptional.isPresent()) {
            Tags tag = tagOptional.get();
            tag.setTagName(tagName);
            tagRepository.save(tag);
            return CreateApiResponse.createResponse(tag,false);
        } else {
            throw new ApiException(ErrorCode.NOT_FOUND.getStatusCode().value(),"Tag not found");
        }
    }

    @Override
    public void deleteTag(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag not found");
            return;
        }
        tagRepository.deleteById(tagId);
        ResponseEntity.ok("Tag deleted successfully");
    }


    @Override
    public List<Tags> getAllTags() {
        return tagRepository.findAll();
    }
}
