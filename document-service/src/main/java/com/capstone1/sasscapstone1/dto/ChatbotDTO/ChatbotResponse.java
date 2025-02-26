package com.capstone1.sasscapstone1.dto.ChatbotDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class ChatbotResponse {
    private List<PartDTO> parts;
    private String role;
    private String filePath;
    private String fileName;
    private String subjectName;
    private String userName;
    private Long docId;
}
