package sk.stuba.pks.old.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.dto.PacketBuilder;
import sk.stuba.pks.old.service.PacketReceiveListener;
import sk.stuba.pks.old.service.PacketSender;

@Service
public class AckKeepAliveService implements PacketReceiveListener {

    @Autowired
    private PacketSender packetSender;

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet.isKeepAlive() && !packet.isAck()) {
            packetSender.getPacketQueue().addFirst(PacketBuilder.ackKeepAlivePacket(packet.getSessionId(), packet.getSequenceNumber()));
        }
    }
}
