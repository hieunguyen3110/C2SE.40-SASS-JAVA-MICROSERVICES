package com.capstone1.sasscapstone1.controller.FacultyController;

import com.capstone1.sasscapstone1.dto.FacultyDto.FacultyDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.service.FacultyService.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;
    @GetMapping("/{facultyId}")
    public ApiResponse<FacultyDto> findByFacultyId(@PathVariable("facultyId") long facultyId) throws Exception {
        return facultyService.getFacultyById(facultyId);
    }
}
