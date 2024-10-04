package sk.stuba.pks.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.stuba.pks.dto.Packet;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleMessage.class, name = "simple"),
        @JsonSubTypes.Type(value = FileMessage.class, name = "file"),
        @JsonSubTypes.Type(value = SynMessage.class, name = "syn")
})
public interface Message {
    MessageType getType();
    Packet getParentPacket();


    default public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting SynMessage to JSON", e);
        }
    }
}