package sk.stuba.pks.old.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.util.Base64Deserializer;
import sk.stuba.pks.old.util.Base64Serializer;

@JsonTypeName("file")
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

    public FileMessage() {
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }

    public String getFileName() {
        return this.fileName;
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

    public byte[] getPayload() {
        return this.payload;
    }

    public Packet getParentPacket() {
        return this.parentPacket;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    @JsonDeserialize(using = Base64Deserializer.class)
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @JsonIgnore
    public void setParentPacket(Packet parentPacket) {
        this.parentPacket = parentPacket;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FileMessage)) return false;
        final FileMessage other = (FileMessage) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$fileName = this.getFileName();
        final Object other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) return false;
        if (this.getNumberOfPackets() != other.getNumberOfPackets()) return false;
        if (this.getLocalMessageId() != other.getLocalMessageId()) return false;
        if (this.getLocalMessageOffset() != other.getLocalMessageOffset()) return false;
        if (!java.util.Arrays.equals(this.getPayload(), other.getPayload())) return false;
        final Object this$parentPacket = this.getParentPacket();
        final Object other$parentPacket = other.getParentPacket();
        if (this$parentPacket == null ? other$parentPacket != null : !this$parentPacket.equals(other$parentPacket))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FileMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $fileName = this.getFileName();
        result = result * PRIME + ($fileName == null ? 43 : $fileName.hashCode());
        result = result * PRIME + this.getNumberOfPackets();
        result = result * PRIME + this.getLocalMessageId();
        result = result * PRIME + this.getLocalMessageOffset();
        result = result * PRIME + java.util.Arrays.hashCode(this.getPayload());
        final Object $parentPacket = this.getParentPacket();
        result = result * PRIME + ($parentPacket == null ? 43 : $parentPacket.hashCode());
        return result;
    }

    public String toString() {
        return "FileMessage(fileName=" + this.getFileName() + ", numberOfPackets=" + this.getNumberOfPackets() + ", localMessageId=" + this.getLocalMessageId() + ", localMessageOffset=" + this.getLocalMessageOffset() + ", payload=" + java.util.Arrays.toString(this.getPayload()) + ", parentPacket=" + this.getParentPacket() + ")";
    }
}