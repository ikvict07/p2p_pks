package sk.stuba.pks.service.states;

import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.model.Message;
import sk.stuba.pks.model.MessageType;
import sk.stuba.pks.model.SimpleMessage;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.service.mapping.JsonService;
import sk.stuba.pks.util.PacketUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class WaitingForRequestState implements ServerState {

    private final PacketSender packetSender;

    private final PacketReceiver packetReceiver;

    public WaitingForRequestState(PacketSender packetSender, PacketReceiver packetReceiver) {
        this.packetSender = packetSender;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void start(ServerContext context) throws SocketException {
        System.out.println("Waiting for request state");
        Packet firstPacket = packetReceiver.receive();

        System.out.println("Received packet: " + firstPacket);
        if (firstPacket == null || !firstPacket.isData()) {
            return;
        }


        Message message = JsonService.fromPayload(firstPacket.getPayload());
        if (message == null || message.getType() != MessageType.SIMPLE) {
            return;
        }


        SimpleMessage simpleMessage = (SimpleMessage) message;
        int numberOfPackets = simpleMessage.getNumberOfPackets();


        Map<Integer, Packet> packets = new ConcurrentHashMap<>();
        packets.put(PacketUtils.byteArrayToInt(firstPacket.getSequenceNumber()), firstPacket);


        sendAck(firstPacket, context.getClientAddress(), context.getClientPort());


        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 1; i < numberOfPackets; i++) {
                executorService.submit(() -> {
                    int attempts = 0;
                    while (packets.size() != numberOfPackets && attempts < 5) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        Packet packet = packetReceiver.receive();
                        if (packet != null && packet.isData() && !packet.isCorrupt() && !packets.containsKey(PacketUtils.byteArrayToInt(packet.getSequenceNumber()))) {
                            int sequenceNumber = PacketUtils.byteArrayToInt(packet.getSequenceNumber());
                            packets.put(sequenceNumber, packet);
                            sendAck(packet, context.getClientAddress(), context.getClientPort());
                        } else {
                            attempts++;
                        }
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (packets.size() != numberOfPackets) {
                throw new SocketException("Not all packets were received");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Packet processing was interrupted", e);
        }


        processPackets(packets);
    }

    private void processPackets(Map<Integer, Packet> packets) {
        packets.entrySet().forEach(
                entry -> {
                    Packet packet = entry.getValue();
                    Message message = JsonService.fromPayload(packet.getPayload());
                    if (message == null) {
                        return;
                    }
                    System.out.println("=====================================");
                    System.out.println(message);
                }
        );
    }


    private void sendAck(Packet packet, String address, int port) {
        PacketBuilder ackPacketBuilder = new PacketBuilder();
        ackPacketBuilder.setSessionId(packet.getSessionId());
        ackPacketBuilder.setSequenceNumber(packet.getSequenceNumber());
        ackPacketBuilder.setAckFlag((byte) 0b10);
        ackPacketBuilder.setPayloadType((byte) 0);
        ackPacketBuilder.setPayloadLength(new byte[]{0x00, 0x00});
        ackPacketBuilder.setPayload(new byte[0]);
        packetSender.sendPacket(ackPacketBuilder.build(), address, port);
    }
}
