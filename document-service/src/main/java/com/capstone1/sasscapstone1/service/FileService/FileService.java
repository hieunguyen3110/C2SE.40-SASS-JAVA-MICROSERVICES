package com.capstone1.sasscapstone1.service.FileService;

import java.io.IOException;
import java.util.List;

public interface FileService {
    String extractFileContent(String filePath) throws IOException;
    List<String> findRestrictedWords(String content) throws IOException;
}
