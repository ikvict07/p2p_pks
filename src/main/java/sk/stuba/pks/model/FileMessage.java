package sk.stuba.pks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.util.Base64Deserializer;
import sk.stuba.pks.util.Base64Serializer;

@JsonTypeName("file")
@Data
public class FileMessage implements Message {
    private String fileName;
    private int numberOfPackets;
    private int localMessageId;
    private int localMessageOffset;

    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] payload;

    @JsonIgnore
    private Packet parentPacket;

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }
}