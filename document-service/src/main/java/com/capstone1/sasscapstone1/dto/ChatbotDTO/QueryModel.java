package com.capstone1.sasscapstone1.dto.ChatbotDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class QueryModel {
    private String improved_answer;
    private String reference_document;
}
