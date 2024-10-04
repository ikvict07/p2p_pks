package sk.stuba.pks.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.service.PacketReceiveListener;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.util.PacketUtils;

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
        System.out.println("Sending keep alive packet");
        packetSender.sendPacket(PacketBuilder.keepAlivePacket(session.getSessionId(), sequenceNumber), context.getRemoteIp(), context.getRemotePort());
    }

    public Executor startKeepAliveTask(Session session) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Sending keep alive packet");
            keepAlive(session);
        }, 3, 10, TimeUnit.SECONDS);
        return scheduler;
    }


    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isKeepAlive() || !packet.isAck()) return;
        receivedPacket.set(true);
        sequenceNumber = PacketUtils.incrementSequenceNumber(sequenceNumber);
    }
}
