package com.capstone1.sasscapstone1.dto.SearchUserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserResponseDto {
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private String facultyName;
    private String major;
    private List<String> roles;
}
