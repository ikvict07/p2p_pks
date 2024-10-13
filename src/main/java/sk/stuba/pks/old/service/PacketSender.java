package sk.stuba.pks.old.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.service.states.session.Context;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class PacketSender implements Sender {

    private final DatagramSocket socket;

    @Getter
    @Setter
    private Deque<Packet> packetQueue = new ConcurrentLinkedDeque<>();
    public PacketSender(DatagramSocket socket) {
        this.socket = socket;
    }

    @Autowired
    private Context context;

    @Override
    public void sendPacket(Packet packet, String serverAddress, int serverPort) {
        try {
            byte[] data = packet.getBytes();
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, address, serverPort);
            socket.send(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSendingPackets() {
        Runnable task = () -> {
            while (true) {
                // ensure thread is not interrupted
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread interrupted");
                    return;
                }
                Packet packet = packetQueue.poll();
                if (packet == null) {
                    continue;
                }
                sendPacket(packet, context.getRemoteIp(), context.getRemotePort());
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
}

