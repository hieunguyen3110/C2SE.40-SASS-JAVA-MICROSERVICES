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
public class ChatbotDto {
    private String responseText;
    private List<String> file_source;
    private String filePath;
    private String fileName;
    private String subjectName;
    private String userName;
    private Long docId;
}
