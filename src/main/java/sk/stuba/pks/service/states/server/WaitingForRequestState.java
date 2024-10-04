package sk.stuba.pks.service.states.server;

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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static sk.stuba.pks.enums.StaticDefinition.WINDOW_SIZE;

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
        final int numberOfPackets = simpleMessage.getNumberOfPackets();

        Map<Integer, Packet> packets = new ConcurrentHashMap<>();
        packets.put(PacketUtils.byteArrayToInt(firstPacket.getSequenceNumber()), firstPacket);

        sendAck(firstPacket, context.getClientAddress(), context.getClientPort());

        Queue<Packet> receivedPackets = new ConcurrentLinkedQueue<>();
        ExecutorService executorService = Executors.newCachedThreadPool();

        while (packets.size() != numberOfPackets) {
            for (int i = 0; i < WINDOW_SIZE.value; i++) {
                if (packets.size() == numberOfPackets) break;

                Packet packet = packetReceiver.receive();
                if (packet != null && packet.isData()) {
                    packets.put(PacketUtils.byteArrayToInt(packet.getSequenceNumber()), packet);
                    receivedPackets.add(packet);
                }
            }

            executorService.submit(() -> {
                for (int i = 0; i < WINDOW_SIZE.value; i++) {
                    Packet packet = receivedPackets.poll();
                    if (packet != null) {
                        sendAck(packet, context.getClientAddress(), context.getClientPort());
                    }
                }
            });
        }
        System.out.println("OK");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            executorService.close();
        }

        processPackets(packets);
        System.out.println("All packets received");
        System.out.println(packets.size());
        context.setState(ServerStateType.WAITING_FOR_SYN);
    }

    private void processPackets(Map<Integer, Packet> packets) {
        packets.forEach((key, packet) -> {
            Message message = JsonService.fromPayload(packet.getPayload());
            if (message == null) {
                return;
            }
//            System.out.println("=====================================");
//            System.out.println(message);
        });
    }


    private void sendAck(Packet packet, String address, int port) {
        PacketBuilder ackPacketBuilder = new PacketBuilder();
        ackPacketBuilder.setSessionId(packet.getSessionId());
        ackPacketBuilder.setSequenceNumber(packet.getSequenceNumber());
        ackPacketBuilder.setAckFlag((byte) 0b01);
        ackPacketBuilder.setPayloadType((byte) 0);
        ackPacketBuilder.setPayloadLength(new byte[]{0x00, 0x00});
        ackPacketBuilder.setPayload(new byte[0]);
        packetSender.sendPacket(ackPacketBuilder.build(), address, port);
    }
}
