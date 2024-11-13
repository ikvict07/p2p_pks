package sk.stuba.pks.library.dto;

import sk.stuba.pks.library.util.PacketUtils;

import java.util.Arrays;

public class Packet {
    private byte[] sessionId; // 4 bytes
    private byte[] sequenceNumber; // 4 bytes
    private byte flags; // 4 bits for ackFlag and 4 bits for payloadType : AckFlag: 00-None, 01-Ack, 10-Syn, 11-SynAck; PayloadType: 00-Data, 01-KeepAlive, 10 – FIN
    private byte[] payloadLength; // 2 bytes
    private byte[] payload; // 0-1476 bytes
    private byte[] checksum; // 4 bytes

    protected Packet() {
    }

    public void setSessionId(byte[] sessionId) {
        if (sessionId.length != 4) {
            throw new IllegalArgumentException("sessionId must be 4 bytes long");
        }
        this.sessionId = sessionId;
    }

    public void setSequenceNumber(byte[] sequenceNumber) {
        if (sequenceNumber.length != 4) {
            throw new IllegalArgumentException("sequenceNumber must be 4 bytes long");
        }
        this.sequenceNumber = sequenceNumber;
    }

    public void setPayloadLength(byte[] payloadLength) {
        if (payloadLength.length != 2) {
            throw new IllegalArgumentException("payloadLength must be 2 bytes long");
        }
        this.payloadLength = payloadLength;
    }

    public byte getAckFlag() {
        return (byte) (flags & 0x03); // Get the first 2 bits (bits 0 and 1: 0011)
    }

    public void setAckFlag(byte ackFlag) {
        if (ackFlag < 0 || ackFlag > 3) {
            throw new IllegalArgumentException("ackFlag must be between 0 and 3");
        }
        flags = (byte) ((flags & 0xFC) | (ackFlag & 0x03)); // Set the first 2 bits
    }

    public byte getPayloadType() {
        return (byte) ((flags >> 2) & 0x03); // Get the next 2 bits (bits 2 and 3: 1100)
    }

    public void setPayloadType(byte payloadType) {
        if (payloadType < 0 || payloadType > 3) {
            throw new IllegalArgumentException("payloadType must be between 0 and 3");
        }
        flags = (byte) ((flags & 0xF3) | ((payloadType & 0x03) << 2)); // Set the next 2 bits
    }

    public void setPayload(byte[] payload) {
        if (payload.length > 1476) {
            throw new IllegalArgumentException("payload must be at most 1476 bytes long, but was " + payload.length + " bytes long");
        }
        this.payload = payload;
    }

    public void setChecksum(byte[] checksum) {
        if (checksum.length != 2) {
            throw new IllegalArgumentException("checksum must be 2 bytes long");
        }
        this.checksum = checksum;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[4 + 4 + 1 + 2 + payload.length + 2];
        populateBytesWithoutChecksum(bytes);
        System.arraycopy(checksum, 0, bytes, 11 + payload.length, 2);
        return bytes;
    }

    public byte[] getBytesWithoutChecksum() {
        byte[] bytes = new byte[4 + 4 + 1 + 2 + payload.length];
        populateBytesWithoutChecksum(bytes);
        return bytes;
    }

    private void populateBytesWithoutChecksum(byte[] bytes) {
        System.arraycopy(sessionId, 0, bytes, 0, 4);
        System.arraycopy(sequenceNumber, 0, bytes, 4, 4);
        bytes[8] = flags;
        System.arraycopy(payloadLength, 0, bytes, 9, 2);
        System.arraycopy(payload, 0, bytes, 11, payload.length);
    }

    public String toString() {
        return """
                Session ID: %s
                Sequence Number: %s
                Ack Flag: %d
                Payload Type: %d
                Payload Length: %s
                Checksum: %s
                Payload: %s
                """.formatted(
                PacketUtils.bytesToHex(sessionId),
                PacketUtils.bytesToHex(sequenceNumber),
                getAckFlag(),
                getPayloadType(),
                PacketUtils.bytesToHex(payloadLength),
                PacketUtils.bytesToHex(checksum),
                new String(payload));
    }

    public boolean isAck() {
        return (getAckFlag() & 0x01) == 1;
    }

    public boolean isSyn() {
        return (getAckFlag() & 0x02) == 2;
    }

    public boolean isSynAck() {
        return (getAckFlag() & 0x03) == 3;
    }

    public boolean isData() {
        return getPayloadType() == 0;
    }

    public boolean isKeepAlive() {
        return (getPayloadType() & 0b01) == 1;
    }

    public boolean isCorrupt() {
        return !Arrays.equals(checksum, PacketUtils.generateChecksum(this));
    }

    public boolean isFin() {
        return (getPayloadType() & 0b10) == 2;
    }

    public byte[] getSessionId() {
        return this.sessionId;
    }

    public byte[] getSequenceNumber() {
        return this.sequenceNumber;
    }

    public byte getFlags() {
        return this.flags;
    }

    public byte[] getPayloadLength() {
        return this.payloadLength;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] getChecksum() {
        return this.checksum;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Packet)) return false;
        final Packet other = (Packet) o;
        if (!other.canEqual((Object) this)) return false;
        if (!Arrays.equals(this.getSessionId(), other.getSessionId())) return false;
        if (!Arrays.equals(this.getSequenceNumber(), other.getSequenceNumber())) return false;
        if (this.getFlags() != other.getFlags()) return false;
        if (!Arrays.equals(this.getPayloadLength(), other.getPayloadLength())) return false;
        if (!Arrays.equals(this.getPayload(), other.getPayload())) return false;
        if (!Arrays.equals(this.getChecksum(), other.getChecksum())) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Packet;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + Arrays.hashCode(this.getSessionId());
        result = result * PRIME + Arrays.hashCode(this.getSequenceNumber());
        result = result * PRIME + this.getFlags();
        result = result * PRIME + Arrays.hashCode(this.getPayloadLength());
        result = result * PRIME + Arrays.hashCode(this.getPayload());
        result = result * PRIME + Arrays.hashCode(this.getChecksum());
        return result;
    }
}