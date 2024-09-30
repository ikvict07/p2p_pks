package sk.stuba.pks.dto;

import sk.stuba.pks.util.PacketUtils;

public class PacketBuilder {
    private final Packet packet = new Packet();
    private int checksum = 0;

    public PacketBuilder setSessionId(byte[] sessionId) {
        packet.setSessionId(sessionId);
        checksum = checksum | 1;
        return this;
    }

    public PacketBuilder setSequenceNumber(byte[] sequenceNumber) {
        packet.setSequenceNumber(sequenceNumber);
        checksum = checksum | 1 << 1;
        return this;
    }

    public PacketBuilder setAckFlag(byte ackFlag) {
        packet.setAckFlag(ackFlag);
        checksum = checksum | 1 << 2;
        return this;
    }

    public PacketBuilder setPayloadType(byte payloadType) {
        packet.setPayloadType(payloadType);
        checksum = checksum | 1 << 3;
        return this;
    }

    public PacketBuilder setPayloadLength(byte[] payloadLength) {
        packet.setPayloadLength(payloadLength);
        checksum = checksum | 1 << 4;
        return this;
    }


    public PacketBuilder setPayload(byte[] payload) {
        packet.setPayload(payload);
        checksum = checksum | 1 << 5;
        return this;
    }

    public PacketBuilder setChecksum(byte[] checksum) {
        packet.setChecksum(checksum);
        this.checksum = this.checksum | 1 << 6;
        return this;
    }

    public Packet build() {
        return switch (checksum) {
            case 0b111111 -> {
                setChecksum(PacketUtils.generateChecksum(packet));
                yield packet;
            }
            case 0b1111111 -> packet;

            default -> throw new IllegalStateException("Not all fields are set");
        };
    }
}
