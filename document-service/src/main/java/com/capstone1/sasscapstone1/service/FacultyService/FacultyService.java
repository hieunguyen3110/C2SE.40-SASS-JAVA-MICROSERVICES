package com.capstone1.sasscapstone1.service.FacultyService;

import com.capstone1.sasscapstone1.dto.FacultyDto.FacultyDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;

public interface FacultyService {
    ApiResponse<FacultyDto> getFacultyById(long facultyId) throws Exception;
}
