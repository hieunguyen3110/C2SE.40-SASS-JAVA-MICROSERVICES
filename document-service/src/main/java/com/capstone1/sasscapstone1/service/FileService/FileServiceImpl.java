package com.capstone1.sasscapstone1.service.FileService;

import org.springframework.stereotype.Service;
import com.aspose.pdf.Document;
import com.aspose.pdf.TextAbsorber;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {

    private static final String RESTRICTED_WORDS_FILE = "sensitive-words.txt";

    @Override
    public String extractFileContent(String filePath) throws IOException {
        try {
            try (InputStream inputStream = new URL(filePath).openStream()) {
                Document pdfDocument = new Document(inputStream);
                TextAbsorber textAbsorber = new TextAbsorber();
                pdfDocument.getPages().accept(textAbsorber);
                return textAbsorber.getText();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file content: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> findRestrictedWords(String content) throws IOException {
        Set<String> restrictedWords;
        try {
            URL resourceURL = getClass().getClassLoader().getResource(RESTRICTED_WORDS_FILE);
            if (resourceURL == null) {
                throw new IOException("Restricted words file not found");
            }
            try (Stream<String> lines = Files.lines(Paths.get(resourceURL.toURI()))) {
                restrictedWords = lines
                        .map(String::trim)
                        .filter(word -> !word.isBlank())
                        .collect(Collectors.toSet());
            }
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI syntax for restricted words file", e);
        } catch (IOException e) {
            throw new IOException("Error reading restricted words file: " + e.getMessage(), e);
        }

        String normalizedContent = normalizeText(content);

        return restrictedWords.stream()
                .filter(word -> {
                    String normalizedWord = normalizeText(word);
                    return normalizedContent.contains(normalizedWord);
                })
                .collect(Collectors.toList());
    }

    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[\\p{Punct}]", " ") // Loại bỏ dấu câu
                .replaceAll("\\s+", " ")        // Chuẩn hóa khoảng trắng
                .trim();
    }
}
