package sk.stuba.pks.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import sk.stuba.pks.dto.Packet;

@JsonTypeName("syn")
@Data
public class SynMessage implements Message {
    private int port;
    private String address;

    @JsonIgnore
    private Packet parentPacket;


    @Override
    public MessageType getType() {
        return MessageType.SYN;
    }

    @Override
    public Packet getParentPacket() {
        return parentPacket;
    }

}
