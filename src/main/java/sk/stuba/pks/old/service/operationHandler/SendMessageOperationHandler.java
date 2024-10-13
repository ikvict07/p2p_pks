package sk.stuba.pks.old.service.operationHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.dto.PacketBuilder;
import sk.stuba.pks.old.service.PacketReceiver;
import sk.stuba.pks.old.service.PacketSender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

@Service
public class SendMessageOperationHandler implements OperationHandler {
    @Autowired
    private PacketReceiver packetReceiver;

    @Autowired
    private PacketSender packetSender;
    public SendMessageOperationHandler() {
    }

    @Override
    @Async
    public void handle(String operation) {
        if (operation.equals("send")) {
            try (DatagramSocket socket = new DatagramSocket()) {
                PacketBuilder pb = new PacketBuilder();
                pb.setSequenceNumber(new byte[]{0, 0, 0, 1})
                        .setSessionId(new byte[]{0, 0, (byte) 0xFA, 0})
                        .setAckFlag((byte) 2)
                        .setPayloadType((byte) 0);

                //json
                String synMessage = """
                {
                    "type": "syn",
                    "port": 9874,
                    "address": "localhost"
                }
                """;
                byte[] payload = synMessage.getBytes();
                byte[] payloadLength = new byte[]{
                        (byte) (payload.length >> 8),
                        (byte) payload.length};
                pb.setPayloadLength(payloadLength);
                pb.setPayload(payload);

                Packet p = pb.build();
                byte[] data = p.getBytes();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(data, data.length, address, 9875);
                socket.send(packet);
                System.out.println("Sent: " + Arrays.toString(p.getBytes()));


                Packet receivedAck = packetReceiver.receive();
                System.out.println("Received: " + Arrays.toString(receivedAck.getBytes()));


                PacketBuilder pb2 = new PacketBuilder();
                pb2.setSequenceNumber(new byte[]{0, 0, 0, 2})
                        .setSessionId(new byte[]{0, 0, (byte) 0xFA, 0})
                        .setAckFlag((byte) 0b00)
                        .setPayloadType((byte) 0);
                String message = """
                {
                    
                    "type": "simple",
                    "message": "Hello world! this is my first package that is sent from here!",
                    "numberOfPackets": 2
                }
                """;
                byte[] payload2 = message.getBytes();
                byte[] payloadLength2 = new byte[]{
                        (byte) (payload2.length >> 8),
                        (byte) payload2.length};
                pb2.setPayloadLength(payloadLength2);
                pb2.setPayload(payload2);

                packetSender.sendPacket(pb2.build(), "localhost", 9875);
                System.out.println("Sent: " + Arrays.toString(pb2.build().getBytes()));
                receivedAck = packetReceiver.receive();
                System.out.println("Received: " + Arrays.toString(receivedAck.getBytes()));
                PacketBuilder pb3 = new PacketBuilder();
                pb3.setSequenceNumber(new byte[]{0, 0, 0, 3})
                        .setSessionId(new byte[]{0, 0, (byte) 0xFA, 0})
                        .setAckFlag((byte) 0b00)
                        .setPayloadType((byte) 0);
                String message3 = """
                {
                    "type": "simple",
                    "message": "Hello world! this is my second package that is sent from here!",
                    "numberOfPackets": 2
                }
                """;
                byte[] payload3 = message3.getBytes();
                byte[] payloadLength3 = new byte[]{
                        (byte) (payload2.length >> 8),
                        (byte) payload2.length};
                pb3.setPayloadLength(payloadLength3);
                pb3.setPayload(payload3);

                packetSender.sendPacket(pb3.build(), "localhost", 9875);
                System.out.println("Sent: " + Arrays.toString(pb3.build().getBytes()));
                receivedAck = packetReceiver.receive();
                System.out.println("Received: " + Arrays.toString(receivedAck.getBytes()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
