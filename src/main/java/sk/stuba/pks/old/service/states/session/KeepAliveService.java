package sk.stuba.pks.old.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.dto.PacketBuilder;
import sk.stuba.pks.old.service.PacketReceiveListener;
import sk.stuba.pks.old.service.PacketSender;
import sk.stuba.pks.old.util.PacketUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class KeepAliveService implements PacketReceiveListener {
    private byte[] sequenceNumber = new byte[]{0, 0, 0, 0};
    @Autowired
    private PacketSender packetSender;
    @Autowired
    private Context context;

    private AtomicBoolean receivedPacket = new AtomicBoolean(true);

    private void keepAlive(Session session) {
        if (!receivedPacket.get()) {
            receivedPacket.set(false);
            System.out.println("Connection lost");
            throw new RuntimeException("Connection lost");
        }
        receivedPacket.set(false);
        packetSender.getPacketQueue().addFirst(PacketBuilder.keepAlivePacket(session.getSessionId(), sequenceNumber));
        System.out.println("Keep alive sent");
//        packetSender.sendPacket(PacketBuilder.keepAlivePacket(session.getSessionId(), sequenceNumber), context.getRemoteIp(), context.getRemotePort());
    }

    public Executor startKeepAliveTask(Session session) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            keepAlive(session);
        }, 3, 10, TimeUnit.SECONDS);
        return scheduler;
    }


    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isKeepAlive()) return;

        if (packet.isAck()) {
            System.out.println("Keep alive received");
            receivedPacket.set(true);
            sequenceNumber = PacketUtils.incrementSequenceNumber(sequenceNumber);
        } else {
            packetSender.getPacketQueue().addFirst(
                    PacketBuilder.keepAliveAckPacket(packet.getSessionId(), packet.getSequenceNumber()));
        }

    }
}
