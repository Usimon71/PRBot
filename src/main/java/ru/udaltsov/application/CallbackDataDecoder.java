package ru.udaltsov.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class CallbackDataDecoder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode decode(String encodedData) throws JsonProcessingException {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
        String json = new String(decodedBytes, StandardCharsets.UTF_8);

        // Convert JSON to JsonNode
        return objectMapper.readTree(json);
    }
}
