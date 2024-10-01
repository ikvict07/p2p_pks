package sk.stuba.pks.service.states;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.model.Message;
import sk.stuba.pks.model.SynMessage;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.service.mapping.JsonService;

@Service
@Log4j2
public class WaitingForSynState implements ServerState {
    private final PacketSender packetSender;

    private final PacketReceiver packetReceiver;

    public WaitingForSynState(PacketReceiver packetReceiver, PacketSender packetSender) {
        this.packetReceiver = packetReceiver;
        this.packetSender = packetSender;
    }

    @Override
    public void start(ServerContext context) {
        Packet request = packetReceiver.receive();
        if (request == null) {
            log.info("Received packet is null");
            return;
        }
        if (!request.isSyn()) {
            log.info("Received packet is not SYN");
            return;
        }
        Message message = JsonService.fromPayload(request.getPayload());

        if (!(message instanceof SynMessage synMessage)) {
            log.info("Received message is not SYN");
            return;
        }

        PacketBuilder packetBuilder = new PacketBuilder();
        packetBuilder
                .setSessionId(request.getSessionId())
                .setSequenceNumber(request.getSequenceNumber())
                .setAckFlag((byte) 0x10)
                .setPayloadLength(new byte[]{0x00, 0x00})
                .setPayload(new byte[0]);
        Packet response = packetBuilder.build();
        String address = synMessage.getAddress();
        int port = synMessage.getPort();
        packetSender.sendPacket(response, address, port);
        context.setState(ServerStateType.WAITING_FOR_ACK);

    }
}