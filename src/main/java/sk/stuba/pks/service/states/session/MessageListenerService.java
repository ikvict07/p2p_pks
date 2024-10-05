package sk.stuba.pks.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.model.Message;
import sk.stuba.pks.model.SimpleMessage;
import sk.stuba.pks.service.PacketReceiveListener;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.service.mapping.JsonService;
import sk.stuba.pks.util.PacketUtils;

@Service
public class MessageListenerService implements PacketReceiveListener {

    @Autowired
    private PacketSender packetSender;

    @Autowired
    private Context context;

    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isData()) return;
        if (PacketUtils.bytesToInt(packet.getPayloadLength()) == 0) return;

        Message message = JsonService.fromPayload(packet.getPayload());
        if (!(message instanceof SimpleMessage simpleMessage)) return;

        packetSender.sendPacket(PacketBuilder.ackPacket(packet.getSessionId(), packet.getSequenceNumber()), context.getRemoteIp(), context.getRemotePort());
    }
}
