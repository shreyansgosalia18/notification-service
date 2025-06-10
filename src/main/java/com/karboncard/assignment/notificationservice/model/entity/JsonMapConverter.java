package com.karboncard.assignment.notificationservice.model.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            if (attribute == null) {
                return null;
            }

            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(objectMapper.writeValueAsString(attribute));
            return pgObject;
        } catch (Exception e) {
            throw new RuntimeException("Error converting map to jsonb", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(Object dbData) {
        try {
            if (dbData == null) {
                return null;
            }

            if (dbData instanceof PGobject) {
                String value = ((PGobject) dbData).getValue();
                if (value == null) {
                    return null;
                }
                return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error converting jsonb to map", e);
        }
    }
}