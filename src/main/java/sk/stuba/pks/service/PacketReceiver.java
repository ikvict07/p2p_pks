package sk.stuba.pks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Service
public class PacketReceiver implements Receiver {
    private final DatagramSocket socket;

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
}
