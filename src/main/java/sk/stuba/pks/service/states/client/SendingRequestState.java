package sk.stuba.pks.service.states.client;

import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.enums.StaticDefinition;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.util.PacketUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SendingRequestState implements ClientState {
    private final PacketSender packetSender;
    private final PacketReceiver packetReceiver;

    private byte[] sessionId;

    public SendingRequestState(PacketSender packetSender, PacketReceiver packetReceiver) {
        this.packetSender = packetSender;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void start(ClientContext context) throws SocketException {
        sessionId = context.getSessionId();
        int packetNumber = 2;
        Queue<Packet> packetsToSend = generatePacketList(packetNumber);
        Packet firstPacket = packetsToSend.poll();
        packetSender.sendPacket(firstPacket, context.getServerAddress(), context.getServerPort());
        byte[] ack = receiveAck();
        assert firstPacket != null;
        if (!Arrays.equals(ack, firstPacket.getSequenceNumber())) {
            throw new SocketException("Invalid ack");
        }

        Map<Integer, Packet> confirmed = new ConcurrentHashMap<>();
        Queue<Packet> unconfirmed = new ArrayDeque<>();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(() -> {
            while (true) {
                Packet ackPacket = packetReceiver.receive();
                if (ackPacket == null) {
                    continue;
                }
//                System.out.println("Received: " + ackPacket);
                if (Arrays.equals(ackPacket.getSessionId(), sessionId) && ackPacket.isAck()) {
                    confirmed.put(PacketUtils.byteArrayToInt(ackPacket.getSequenceNumber()), ackPacket);
                }
            }
        });
        ScheduledExecutorService regularSender = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService periodicResender = Executors.newSingleThreadScheduledExecutor();

        Runnable regularSendTask = () -> {
//            System.out.println("Im regular send task");
//            System.out.println("Packets to send: " + packetsToSend.size());
//            System.out.println("Unconfirmed: " + unconfirmed.size());
            if (!packetsToSend.isEmpty()) {
                for (int i = 0; i < StaticDefinition.WINDOW_SIZE.value; i++) {
                    Packet packet = packetsToSend.poll();
                    if (packet == null) {
                        break;
                    }
                    unconfirmed.add(packet);
                    packetSender.sendPacket(packet, context.getServerAddress(), context.getServerPort());
                }

            }
        };

        Runnable periodicResendTask = () -> {
//            System.out.println("Im periodic resend task");
//            System.out.println("Unconfirmed: " + unconfirmed.size());
            for (Packet packet : unconfirmed) {
                if (confirmed.containsKey(PacketUtils.byteArrayToInt(packet.getSequenceNumber()))) {
                    unconfirmed.remove(packet);
                } else {
                    packetsToSend.add(packet);
                }
            }
            if (confirmed.size() == packetNumber - 1) {
                periodicResender.shutdown();
            }
        };


        regularSender.scheduleAtFixedRate(regularSendTask, 0, 100, TimeUnit.MILLISECONDS);


        periodicResender.scheduleAtFixedRate(periodicResendTask, 200, 200, TimeUnit.MILLISECONDS);

        while (confirmed.size() != packetNumber - 1) {
            Thread.onSpinWait();
        }

//        System.out.println("Exited while");
//        System.out.println("Confirmed: " + confirmed.size());
        regularSender.shutdown();
        periodicResender.shutdown();
        try {
            if (!regularSender.awaitTermination(10, TimeUnit.SECONDS)) {
                regularSender.shutdownNow();
            }
            if (!periodicResender.awaitTermination(10, TimeUnit.SECONDS)) {
                periodicResender.shutdownNow();
            }
        } catch (InterruptedException e) {
            regularSender.shutdownNow();
            periodicResender.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            if (confirmed.size() != packetNumber - 1) {
                executorService.shutdown();
                executorService.close();
                regularSender.close();
                periodicResender.close();
                throw new SocketException("Not all packets confirmed");
            }
        }

        throw new SocketException("All packets sent");
    }

    private byte[] receiveAck() {
        Packet ackPacket = null;
        while (ackPacket == null ||
                !Arrays.equals(ackPacket.getSessionId(), sessionId) ||
                !ackPacket.isAck()) {
            ackPacket = packetReceiver.receive();
//            System.out.println("Received: " + ackPacket);
//            System.out.println(ackPacket == null);
//            System.out.println(!Arrays.equals(ackPacket.getSessionId(), sessionId));
//            System.out.println(!ackPacket.isAck());

        }
//        System.out.println("AND THIS IS CORRECT ACK");
        return ackPacket.getSequenceNumber();
    }

    private Queue<Packet> generatePacketList(int numberOfPackets) {
        Queue<Packet> list = new ArrayDeque<>();

        byte[] sequenceNumber = new byte[]{0, 0, 0, 3};
        for (int i = 0; i < numberOfPackets; i++) {
            PacketBuilder packetBuilder = new PacketBuilder();
            byte[] copy = Arrays.copyOf(sequenceNumber, sequenceNumber.length);
            packetBuilder.setSessionId(sessionId)
                    .setSequenceNumber(copy)
                    .setAckFlag((byte) 0)
                    .setPayloadType((byte) 0);
            String message = "A".repeat(i);
            String payload =
                    // language=JSON
                    """
                            {
                                "type": "simple",
                                "numberOfPackets": "%d",
                                "message": "Hello world! %s"
                            }
                            """.formatted(numberOfPackets, message);
            byte[] payloadBytes = payload.getBytes();
            byte[] payloadLength = new byte[]{(byte) (payloadBytes.length >> 8), (byte) payloadBytes.length};
            packetBuilder.setPayloadLength(payloadLength)
                    .setPayload(payloadBytes);
            list.add(packetBuilder.build());
            incrementSequenceNumber(sequenceNumber);
        }
//        System.out.println("Generated packets: " + list);
        return list;
    }

    private void incrementSequenceNumber(byte[] sequenceNumber) {
        for (int i = sequenceNumber.length - 1; i >= 0; i--) {
            if (sequenceNumber[i] == (byte) 0xFF) {
                sequenceNumber[i] = 0;
            } else {
                sequenceNumber[i]++;
                break;
            }
        }
    }
}
