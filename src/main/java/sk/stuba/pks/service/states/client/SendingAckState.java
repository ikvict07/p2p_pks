package sk.stuba.pks.service.states.client;

import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;

@Service
public class SendingAckState implements ClientState {
    private final PacketSender packetSender;
    private final PacketReceiver packetReceiver;

    private byte[] sessionId;
    public SendingAckState(PacketSender packetSender, PacketReceiver packetReceiver) {
        this.packetSender = packetSender;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void start(ClientContext context) throws SocketException {
        sessionId = context.getSessionId();
        sendAckPacket(context.getServerAddress(), context.getServerPort());
        context.setClientState(ClientStateType.SENDING_REQUEST);
    }

    private void sendAckPacket(String serverAddress, int serverPort) throws SocketException {
        PacketBuilder synPacketBuilder = new PacketBuilder();
        synPacketBuilder.setSessionId(sessionId)
                .setSequenceNumber(new byte[]{0, 0, 0, 2})
                .setAckFlag((byte) 1)
                .setPayloadType((byte) 0)
                .setPayloadLength(new byte[]{0, 0})
                .setPayload(new byte[0]);
        packetSender.sendPacket(synPacketBuilder.build(), serverAddress, serverPort);
    }
}
