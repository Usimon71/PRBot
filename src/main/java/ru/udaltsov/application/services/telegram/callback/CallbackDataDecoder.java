package ru.udaltsov.application.services.telegram.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class CallbackDataDecoder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, String> decode(String encodedData) throws JsonProcessingException {
       String[] decodedArr = encodedData.split(":");
        Map<String, String> result = new HashMap<>();
        result.put("type", decodedArr[0]);
        result.put("value", decodedArr[1]);
        result.put("chatid", decodedArr[2]);

       return result;
    }
}
