package sk.stuba.pks;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sk.stuba.pks.annotation.DefaultOperationHandlerQualifier;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.service.operationHandler.OperationHandler;
import sk.stuba.pks.util.PacketUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Log4j2
@Component
public class PksApplicationRunner implements ApplicationRunner {

    private final OperationHandler operationHandler;

    @Autowired
    @DefaultOperationHandlerQualifier
    public PksApplicationRunner(OperationHandler operationHandler) {
        this.operationHandler = operationHandler;
    }

    @Override
    @Async
    public void run(ApplicationArguments args) {
        String operationToDo = "ask";

        System.out.println("LISTENING ON PORT: " + "ff");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (!operationToDo.equalsIgnoreCase("exit")) {
                operationToDo = reader.readLine();
                operationHandler.handle(operationToDo);
            }

        } catch (Exception e) {
            log.error("Couldnt read from console");
        }
        PacketBuilder packetB = new PacketBuilder();
        packetB.setSessionId(new byte[]{0, 0, 1, 1})
                .setSequenceNumber(new byte[]{0, 15, 0, 1})
                .setAckFlag((byte) 2)
                .setPayloadType((byte) 0)
                .setPayloadLength(new byte[]{(byte) 0xAF, 0})
                .setPayload(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Packet packet = packetB.build();
        log.info(packet);
        log.info(PacketUtils.bytesToHex(packet.getBytes()));
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
