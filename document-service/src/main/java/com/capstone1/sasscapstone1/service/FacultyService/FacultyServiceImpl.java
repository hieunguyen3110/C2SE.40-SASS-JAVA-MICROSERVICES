package com.capstone1.sasscapstone1.service.FacultyService;

import com.capstone1.sasscapstone1.dto.FacultyDto.FacultyDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Faculty;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {
    private final FacultyRepository facultyRepository;

    @Override
    public ApiResponse<FacultyDto> getFacultyById(long facultyId) throws Exception {
        try{
            Faculty faculty= facultyRepository.findByFacultyId(facultyId)
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Faculty is not found"));
            return CreateApiResponse.createResponse(FacultyDto.builder()
                            .facultyId(faculty.getFacultyId())
                            .facultyName(faculty.getFacultyName())
                    .build(), false);
        }catch (Exception e){
            throw new Exception("Error when try get faculty by id");
        }
    }
}
