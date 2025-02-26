package com.capstone1.sasscapstone1.controller.TagController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Tags;
import com.capstone1.sasscapstone1.service.TagService.TagService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @PostMapping("/create")
    public ApiResponse<Tags> createTag(@RequestParam String tagName) throws Exception {
        return tagService.createTag(tagName);
    }

    @PutMapping("/update/{tagId}")
    public ApiResponse<Tags> updateTag(@PathVariable Long tagId, @RequestParam String tagName) {
        return tagService.updateTag(tagId, tagName);
    }

    @DeleteMapping("/delete/{tagId}")
    public ApiResponse<String> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return CreateApiResponse.createResponse("Tag deleted successfully",false);
    }

    @GetMapping("/all")
    public ApiResponse<List<Tags>> getAllTags() {
        return CreateApiResponse.createResponse(tagService.getAllTags(),false);
    }
}
