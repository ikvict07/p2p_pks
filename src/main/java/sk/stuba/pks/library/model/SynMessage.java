package sk.stuba.pks.library.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import sk.stuba.pks.library.dto.Packet;

@JsonTypeName("syn")
public class SynMessage implements Message {
    private int port;
    private String address;

    @JsonIgnore
    private Packet parentPacket;

    public SynMessage() {
    }


    @Override
    public MessageType getType() {
        return MessageType.SYN;
    }

    @Override
    public Packet getParentPacket() {
        return parentPacket;
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }

    public int getPort() {
        return this.port;
    }

    public String getAddress() {
        return this.address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @JsonIgnore
    public void setParentPacket(Packet parentPacket) {
        this.parentPacket = parentPacket;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SynMessage)) return false;
        final SynMessage other = (SynMessage) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPort() != other.getPort()) return false;
        final Object this$address = this.getAddress();
        final Object other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) return false;
        final Object this$parentPacket = this.getParentPacket();
        final Object other$parentPacket = other.getParentPacket();
        if (this$parentPacket == null ? other$parentPacket != null : !this$parentPacket.equals(other$parentPacket))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SynMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPort();
        final Object $address = this.getAddress();
        result = result * PRIME + ($address == null ? 43 : $address.hashCode());
        final Object $parentPacket = this.getParentPacket();
        result = result * PRIME + ($parentPacket == null ? 43 : $parentPacket.hashCode());
        return result;
    }

    public String toString() {
        return "SynMessage(port=" + this.getPort() + ", address=" + this.getAddress() + ", parentPacket=" + this.getParentPacket() + ")";
    }
}
