package sk.stuba.pks.service.operationHandler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

@Service
public class RecieveOperationHandler implements OperationHandler {
    public RecieveOperationHandler() {
    }

    @Override
    @Async
    public void handle(String operation) {
        if (operation.equals("recieve")) {
            try (DatagramSocket socket = new DatagramSocket(9876)) {
                byte[] buf = new byte[1500];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] data = packet.getData();
                System.out.println("Received: " + data);
                Packet p = PacketBuilder.getPacketFromBytes(data);
                System.out.println(p);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
