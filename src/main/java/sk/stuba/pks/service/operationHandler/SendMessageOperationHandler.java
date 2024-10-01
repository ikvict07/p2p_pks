package sk.stuba.pks.service.operationHandler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

@Service
public class SendMessageOperationHandler implements OperationHandler {
    public SendMessageOperationHandler() {
    }

    @Override
    @Async
    public void handle(String operation) {
        if (operation.equals("send")) {
            try (DatagramSocket socket = new DatagramSocket()) {
                PacketBuilder pb = new PacketBuilder();
                pb.setSequenceNumber(new byte[]{0, 0, 0, 1})
                        .setSessionId(new byte[]{0, 0, 0, 0})
                        .setAckFlag((byte) 0)
                        .setPayloadType((byte) 0)
                        .setPayloadLength(new byte[]{0, 0})
                        .setPayload(new byte[]{0, 0, 0, 0});
                Packet p = pb.build();
                byte[] data = p.getBytes();
                InetAddress address = InetAddress.getByName("147.175.161.89");
                DatagramPacket packet = new DatagramPacket(data, data.length, address, 8084);
                socket.send(packet);
                System.out.println("Sent: " + Arrays.toString(p.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
