package com.capstone1.sasscapstone1.controller.CronjobController;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.RatingDto.RatingDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.dto.response.DocumentData;
import com.capstone1.sasscapstone1.dto.response.MultipleNewData;
import com.capstone1.sasscapstone1.service.DocumentService.DocumentService;
import com.capstone1.sasscapstone1.service.RatingService.RatingService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cronjob")
@RequiredArgsConstructor
public class CronjobController {
    private final RatingService ratingService;
    private final DocumentService documentService;
    @GetMapping("/ratings/not-checked")
    public ApiResponse<List<RatingDto>> getRatingNotCheckedByDay() throws Exception {
        return CreateApiResponse.createResponse(ratingService.getAllRatingByDayCheckedIsFalse(),false);
    }
    @GetMapping("/collect-data")
    public ApiResponse<MultipleNewData> getDataNew() throws Exception {
        MultipleNewData multipleNewData= new MultipleNewData();
        Map<String,List<DocumentData>> map= documentService.collectNewData();
        multipleNewData.setNewDocuments(map.get("documentsNew"));
        multipleNewData.setUpdateDocuments(map.get("documentUpdate"));
        multipleNewData.setUserInteractions(ratingService.getUserInteractionData());
        return CreateApiResponse.createResponse(multipleNewData,false);
    }
    @GetMapping("/documents/get-by-day")
    public ApiResponse<List<DocumentDto>> getALlDocumentByDay() throws Exception {
        return CreateApiResponse.createResponse(documentService.getAllDocumentByDay(),false);
    }
    @PostMapping("/documents/update-file")
    public ApiResponse<String> updateFileStatus(@RequestBody List<Long> docIds) throws Exception {
        return CreateApiResponse.createResponse(documentService.updateFileStatus(docIds),false);
    }
    @GetMapping("/documents/new-upload")
    public ApiResponse<List<DocumentDto>> getAllNewDocument() throws Exception {
        return CreateApiResponse.createResponse(documentService.getAllNewDocuments(),false);
    }
    @PostMapping("/document/update-status-gen-question")
    public ApiResponse<String> updateStatusGenerateQuestion(@RequestBody List<Long> docIds) throws Exception {
        return CreateApiResponse.createResponse(documentService.updateStatusGenQuestion(docIds), false);
    }
}
