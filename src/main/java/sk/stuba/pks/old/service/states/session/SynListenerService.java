package sk.stuba.pks.old.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.dto.PacketBuilder;
import sk.stuba.pks.old.service.PacketReceiveListener;
import sk.stuba.pks.old.service.PacketSender;

@Service
public class SynListenerService implements PacketReceiveListener {

    @Autowired
    private PacketSender packetSender;

    @Autowired
    private Context context;

    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isSyn()) return;
        System.out.println("Received SYN packet");
        Packet synAckPacket = PacketBuilder.synAckPacket(packet.getSessionId(), packet.getSequenceNumber());
        packetSender.sendPacket(synAckPacket,
                context.getRemoteIp(),
                context.getRemotePort()
        );
    }
}
