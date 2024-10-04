package sk.stuba.pks.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Service
@Log4j2
public class PacketSender implements Sender {

    private final DatagramSocket socket;

    public PacketSender(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void sendPacket(Packet packet, String serverAddress, int serverPort) {
        try {
            byte[] data = packet.getBytes();
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, address, serverPort);
            socket.send(datagramPacket);
            log.info("Sent packet: " + packet);
//            System.out.println("Sent packet: " + packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

