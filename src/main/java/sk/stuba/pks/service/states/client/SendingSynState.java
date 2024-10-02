package sk.stuba.pks.service.states.client;

import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;

import java.util.Arrays;

@Service
public class SendingSynState implements ClientState {

    private final PacketSender packetSender;
    private final PacketReceiver packetReceiver;

    private byte[] sessionId;

    public SendingSynState(PacketSender packetSender, PacketReceiver packetReceiver) {
        this.packetSender = packetSender;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void start(ClientContext context) throws SocketException {
        sessionId = context.getSessionId();
        sendSynPacket(context.getServerAddress(), context.getServerPort());
        receiveSynAckPacket();
        context.setClientState(ClientStateType.SENDING_ACK);
    }

    private void sendSynPacket(String serverAddress, int serverPort) throws SocketException {
        PacketBuilder synPacketBuilder = new PacketBuilder();
        synPacketBuilder.setSessionId(sessionId)
                .setSequenceNumber(new byte[]{0, 0, 0, 0})
                .setAckFlag((byte) 2)
                .setPayloadType((byte) 0);

        String payload = """
                {
                    "address": "localhost",
                    "port": 9874,
                    "type": "syn"
                }
                """;
        byte[] payloadBytes = payload.getBytes();
        byte[] payloadLength = new byte[]{(byte) (payloadBytes.length >> 8), (byte) payloadBytes.length};
        synPacketBuilder.setPayloadLength(payloadLength).setPayload(payloadBytes);
        packetSender.sendPacket(synPacketBuilder.build(), serverAddress, serverPort);
    }

    // TODO: add timeout
    private void receiveSynAckPacket() {
        Packet ackPacket = null;
        while (ackPacket == null ||
                !Arrays.equals(ackPacket.getSessionId(), sessionId) ||
                !ackPacket.isSynAck() ||
                !Arrays.equals(ackPacket.getSequenceNumber(), new byte[]{0, 0, 0, 1})) {
            System.out.println("Waiting for SYN-ACK packet");
            ackPacket = packetReceiver.receive();
            System.out.println("Received: " + ackPacket);
        }
    }
}
