package org.com.elearningservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Converter
@RequiredArgsConstructor
public class JsonToMapConverterLong implements AttributeConverter<Map<Long,String>, String> {
    private final ObjectMapper objectMapper;
    @Override
    public String convertToDatabaseColumn(Map<Long, String> stringObjectMap) {
        try {
            return objectMapper.writeValueAsString(stringObjectMap);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<Long, String> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading JSON to map", e);
        }
    }
}
