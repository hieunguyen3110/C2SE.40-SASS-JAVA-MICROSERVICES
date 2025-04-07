package org.com.batchservice.helpers;

import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Component
@Getter
public class SensitiveWordChecker {
    private final Set<String> sensitiveWords = new HashSet<>();

    public SensitiveWordChecker() {
        loadSensitiveWords();
    }

    private void loadSensitiveWords() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("sensitive-words.txt").getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sensitiveWords.add(line.trim().toLowerCase()); // Chuyển về chữ thường để so sánh không phân biệt hoa thường
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file sensitive_words.txt", e);
        }
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerCaseText = text.toLowerCase(); // Chuyển về chữ thường
        for (String word : sensitiveWords) {
            if (lowerCaseText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
