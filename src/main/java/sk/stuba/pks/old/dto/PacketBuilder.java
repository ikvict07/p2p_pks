package sk.stuba.pks.old.dto;

import sk.stuba.pks.old.util.PacketUtils;

public class PacketBuilder {
    private final Packet packet = new Packet();
    private int checksum = 0;

    public static Packet keepAliveAckPacket(byte[] sessionId, byte[] sequenceNumber) {
        return new PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag((byte) 0b01)
                .setPayloadType((byte) 0b01)
                .setPayloadLength(new byte[] {0, 0})
                .setPayload(new byte[0])
                .build();
    }


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

    public static Packet getPacketFromBytes(byte[] data) {
        PacketBuilder packetBuilder = new PacketBuilder();
        byte[] sessionId = new byte[] {data[0], data[1], data[2], data[3]};
        byte[] sequenceNumber = new byte[] {data[4], data[5], data[6], data[7]};
        byte flags = data[8];
        byte ackFlag = (byte) (flags & 0x03);
        byte payloadType = (byte) ((flags >> 2) & 0x03);
        byte[] payloadLength = new byte[] {data[9], data[10]};
        int payloadLengthInt = PacketUtils.bytesToInt(payloadLength);
        byte[] payload = new byte[payloadLengthInt];
        System.arraycopy(data, 11, payload, 0, payloadLengthInt);
        byte[] checksum = new byte[] {data[11 + payloadLengthInt], data[12 + payloadLengthInt]};
        return packetBuilder.setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag(ackFlag)
                .setPayloadType(payloadType)
                .setPayloadLength(payloadLength)
                .setPayload(payload)
                .setChecksum(checksum)
                .build();
    }

    public static Packet keepAlivePacket(byte[] sessionId, byte[] sequenceNumber) {
        return new PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag((byte) 0b00)
                .setPayloadType((byte) 0b01)
                .setPayloadLength(new byte[] {0, 0})
                .setPayload(new byte[0])
                .build();
    }

    public static Packet ackKeepAlivePacket(byte[] sessionId, byte[] sequenceNumber) {
        return new PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag((byte) 0b01)
                .setPayloadType((byte) 0b01)
                .setPayloadLength(new byte[] {0, 0})
                .setPayload(new byte[0])
                .build();
    }

    public static Packet synPacket(byte[] sessionId, byte[] sequenceNumber, int port, String address) {
        PacketBuilder pb = new PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag((byte) 0b10)
                .setPayloadType((byte) 0b00);
        //language=JSON
        String payload = """
                {
                    "type": "syn",
                    "port": "%d",
                    "address": "%s"
  
                }
                """.formatted(port, address);
        return pb.setPayloadLength(PacketUtils.intToByteArray(payload.length())).setPayload(payload.getBytes()).build();
    }

    public static Packet synAckPacket(byte[] sessionId, byte[] sequenceNumber) {
        return new PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag((byte) 0b11)
                .setPayloadType((byte) 0b00)
                .setPayloadLength(new byte[] {0, 0})
                .setPayload(new byte[0])
                .build();
    }

    public static Packet ackPacket(byte[] sessionId, byte[] sequenceNumber) {
        return new PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(sequenceNumber)
                .setAckFlag((byte) 0b01)
                .setPayloadType((byte) 0b00)
                .setPayloadLength(new byte[] {0, 0})
                .setPayload(new byte[0])
                .build();
    }
}
