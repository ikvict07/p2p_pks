package sk.stuba.pks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import sk.stuba.pks.dto.Packet;

@JsonTypeName("simple")
@Data
public class SimpleMessage implements Message {
    private String message;
    private int numberOfPackets;
    private int localMessageId;
    private int localMessageOffset;
    @JsonIgnore
    private Packet parentPacket;

    @Override
    public MessageType getType() {
        return MessageType.SIMPLE;
    }
}