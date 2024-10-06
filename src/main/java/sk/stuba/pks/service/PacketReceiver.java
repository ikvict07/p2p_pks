package sk.stuba.pks.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@Log4j2
public class PacketReceiver implements Receiver {
    private final DatagramSocket socket;
    private List<PacketReceiveListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(PacketReceiveListener listener) {
        listeners.add(listener);
    }
    public PacketReceiver(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public Packet receive() {
        try {
            byte[] buffer = new byte[1500];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return PacketBuilder.getPacketFromBytes(packet.getData());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Executor startReceivingPackets() {
        Runnable task = () -> {
            while (true) {
                // ensure thread is not interrupted
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread interrupted");
                    return;
                }
                Packet packet = receive();
                if (packet == null) {
                    continue;
                }
                listeners.stream().parallel().forEach(listener -> listener.onPacketReceived(packet));
            }
        };
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(task);
        return executor;
    }
}
