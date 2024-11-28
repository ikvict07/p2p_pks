package sk.stuba.pks.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import sk.stuba.pks.library.dto.Packet;

@JsonTypeName("simple")
public class SimpleMessage implements Message {
    private String message;
    private int numberOfPackets;
    private int localMessageId;
    private int localMessageOffset;
    @JsonIgnore
    private Packet parentPacket;

    public SimpleMessage() {
    }

    @Override
    public MessageType getType() {
        return MessageType.SIMPLE;
    }

    public String getMessage() {
        return this.message;
    }

    public int getNumberOfPackets() {
        return this.numberOfPackets;
    }

    public int getLocalMessageId() {
        return this.localMessageId;
    }

    public int getLocalMessageOffset() {
        return this.localMessageOffset;
    }

    public Packet getParentPacket() {
        return this.parentPacket;
    }

    @Override
    public byte[] getPayload() {
        return getMessage().getBytes();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setNumberOfPackets(int numberOfPackets) {
        this.numberOfPackets = numberOfPackets;
    }

    public void setLocalMessageId(int localMessageId) {
        this.localMessageId = localMessageId;
    }

    public void setLocalMessageOffset(int localMessageOffset) {
        this.localMessageOffset = localMessageOffset;
    }

    @JsonIgnore
    public void setParentPacket(Packet parentPacket) {
        this.parentPacket = parentPacket;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SimpleMessage)) return false;
        final SimpleMessage other = (SimpleMessage) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        if (this.getNumberOfPackets() != other.getNumberOfPackets()) return false;
        if (this.getLocalMessageId() != other.getLocalMessageId()) return false;
        if (this.getLocalMessageOffset() != other.getLocalMessageOffset()) return false;
        final Object this$parentPacket = this.getParentPacket();
        final Object other$parentPacket = other.getParentPacket();
        if (this$parentPacket == null ? other$parentPacket != null : !this$parentPacket.equals(other$parentPacket))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SimpleMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        result = result * PRIME + this.getNumberOfPackets();
        result = result * PRIME + this.getLocalMessageId();
        result = result * PRIME + this.getLocalMessageOffset();
        final Object $parentPacket = this.getParentPacket();
        result = result * PRIME + ($parentPacket == null ? 43 : $parentPacket.hashCode());
        return result;
    }

    public String toString() {
        return "SimpleMessage(message=" + this.getMessage() + ", numberOfPackets=" + this.getNumberOfPackets() + ", localMessageId=" + this.getLocalMessageId() + ", localMessageOffset=" + this.getLocalMessageOffset() + ", parentPacket=" + this.getParentPacket() + ")";
    }
}