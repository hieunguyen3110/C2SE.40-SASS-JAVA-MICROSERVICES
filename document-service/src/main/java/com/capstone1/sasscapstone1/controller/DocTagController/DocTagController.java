package com.capstone1.sasscapstone1.controller.DocTagController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocTag;
import com.capstone1.sasscapstone1.service.DocTagService.DocTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document-tags")
@RequiredArgsConstructor
public class DocTagController {
    private final DocTagService docTagService;

    @PostMapping("/add")
    public ApiResponse<DocTag> addTagToDocument(@RequestParam Long documentId, @RequestParam Long tagId) {
        // Gọi service để thêm tag vào document
        return docTagService.addTagToDocument(documentId, tagId);
    }

    @DeleteMapping("/remove")
    public ApiResponse<String> removeTagFromDocument(@RequestParam Long documentId, @RequestParam Long tagId) throws Exception {
        return docTagService.removeTagFromDocument(documentId, tagId);
    }

    @GetMapping("/{documentId}")
    public ApiResponse<List<DocTag>> getTagsByDocument(@PathVariable Long documentId) {
        return docTagService.getTagsByDocument(documentId);
    }
}
