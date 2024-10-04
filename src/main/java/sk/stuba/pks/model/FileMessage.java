package sk.stuba.pks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import sk.stuba.pks.dto.Packet;

@JsonTypeName("file")
@Data
public class FileMessage implements Message {
    private String name;
    private int offset;
    private int totalParts;
    private String payload;

    @JsonIgnore
    private Packet parentPacket;
    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }
}