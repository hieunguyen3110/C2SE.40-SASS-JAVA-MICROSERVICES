package com.capstone1.sasscapstone1.controller.SubjectController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.dto.SubjectDto.SubjectDto;
import com.capstone1.sasscapstone1.service.SubjectService.SubjectService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/getAllSubject")
    public ApiResponse<?> getAllSubject() {
        List<SubjectDto> subjects = subjectService.getAllSubjects();
        return CreateApiResponse.createResponse(subjects, false);
    }
}
