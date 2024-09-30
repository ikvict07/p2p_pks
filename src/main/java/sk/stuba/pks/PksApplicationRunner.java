package sk.stuba.pks;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.util.PacketUtils;

@Log4j2
@Component
public class PksApplicationRunner implements ApplicationRunner {

    @Override
    @Async
    public void run(ApplicationArguments args) {
        PacketBuilder packetB = new PacketBuilder();
        packetB.setSessionId(new byte[]{0, 0, 1, 1})
                .setSequenceNumber(new byte[]{0, 15, 0, 1})
                .setAckFlag((byte) 2)
                .setPayloadType((byte) 0)
                .setPayloadLength(new byte[]{(byte) 0xAF, 0})
                .setPayload(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Packet packet = packetB.build();
        System.out.println(packet);
        System.out.println(PacketUtils.bytesToHex(packet.getBytes()));
    }

    /*
            try (DatagramSocket socket = new DatagramSocket(9876)) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received: " + receivedMessage);

            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            String response = "Message received";
            byte[] sendData = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            socket.send(sendPacket);
        } catch (SocketException e) {
            log.error("Couldnt connect");
        } catch (IOException e) {
            log.error("Couldnt get message");
        }
     */
}
