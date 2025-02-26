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
public class PartDTO {
    private String text;
    private List<String> file_source;
}
