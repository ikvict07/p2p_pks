package sk.stuba.pks.service.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import sk.stuba.pks.model.Message;

import java.nio.charset.StandardCharsets;


public class JsonService {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonService() {
    }

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static Message parseJson(String json) {
        try {
            return objectMapper.readValue(json, Message.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Message fromPayload(byte[] payload) {
        String json = new String(payload, StandardCharsets.UTF_8);
        return parseJson(json);
    }
}
