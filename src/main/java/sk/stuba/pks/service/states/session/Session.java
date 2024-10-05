package sk.stuba.pks.service.states.session;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.enums.StaticDefinition;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.model.Message;
import sk.stuba.pks.model.SynMessage;
import sk.stuba.pks.service.PacketReceiveListener;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.service.mapping.JsonService;
import sk.stuba.pks.util.IpUtil;
import sk.stuba.pks.util.PacketUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Data
public class Session implements PacketReceiveListener {
    private byte[] sessionId;

    @Autowired
    private Context context;

    @Autowired
    private KeepAliveService keepAliveService;

    @Autowired
    private AckKeepAliveService ackKeepAliveService;

    @Autowired
    private MessageListenerService messageListenerService;

    @Autowired
    private PacketReceiver packetReceiver;

    private final List<Executor> tasks = new ArrayList<>();
    @Autowired
    private PacketSender packetSender;

    private byte[] sequenceNumber = new byte[]{0, 0, 0, 3};

    private Set<Integer> confirmed = ConcurrentHashMap.newKeySet();

    public void createConnection(String remoteIp, int remotePort) {
        context.setMyIp(IpUtil.getIp());
        context.setRemoteIp(remoteIp);
        context.setRemotePort(remotePort);
        sessionId = new byte[]{1, 1, 1, 1};

        packetSender.sendPacket(PacketBuilder.synPacket(sessionId, new byte[]{0, 0, 0, 0}, context.getMyPort(), context.getMyIp()),
                remoteIp,
                remotePort
        );
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Future<Packet> future = scheduler.submit(() -> {
            Packet synAckPacket = null;
            while (synAckPacket == null || !synAckPacket.isSynAck() || !Arrays.equals(synAckPacket.getSessionId(), sessionId)) {
                synAckPacket = packetReceiver.receive();
            }
            return synAckPacket;
        });

        Packet synAckPacket;

        try {
            synAckPacket = future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Timeout: SynAck packet not received within 10 seconds");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            scheduler.shutdown();
        }

        packetSender.sendPacket(PacketBuilder.ackPacket(sessionId, synAckPacket.getSequenceNumber()),
                remoteIp,
                remotePort
        );
        System.out.println("Connection established");
        packetReceiver.addListener(keepAliveService);
        packetReceiver.addListener(ackKeepAliveService);
        packetReceiver.addListener(messageListenerService);
        packetReceiver.addListener(this);
        tasks.add(packetReceiver.startReceivingPackets());
        tasks.add(keepAliveService.startKeepAliveTask(this));
    }

    public void listen() {
        Packet packet = null;
        while (packet == null || !packet.isSyn()) {
            packet = packetReceiver.receive();
        }
        Message message = JsonService.fromPayload(packet.getPayload());
        if (!(message instanceof SynMessage synMessage)) {
            throw new RuntimeException("Received message is not SYN");
        }
        sessionId = packet.getSessionId();
        context.setRemoteIp(synMessage.getAddress());
        context.setRemotePort(synMessage.getPort());
        packetSender.sendPacket(PacketBuilder.synAckPacket(packet.getSessionId(), packet.getSequenceNumber()),
                context.getRemoteIp(),
                context.getRemotePort()
        );


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Future<Packet> future = scheduler.submit(() -> {
            Packet ackPacket = null;
            while (ackPacket == null || !ackPacket.isAck() || !Arrays.equals(ackPacket.getSessionId(), sessionId)) {
                ackPacket = packetReceiver.receive();
            }
            return ackPacket;
        });

        Packet synAckPacket;

        try {
            synAckPacket = future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Timeout: ack packet not received within 10 seconds");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            scheduler.shutdown();
        }
        System.out.println("Connection established");
        packetReceiver.addListener(keepAliveService);
        packetReceiver.addListener(ackKeepAliveService);
        packetReceiver.addListener(messageListenerService);
        packetReceiver.addListener(this);

        tasks.add(packetReceiver.startReceivingPackets());
        tasks.add(keepAliveService.startKeepAliveTask(this));
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            Queue<Packet> packetsToSend = generatePacketList(message);
            ScheduledExecutorService regularSender = Executors.newSingleThreadScheduledExecutor();
            ScheduledExecutorService periodicResender = Executors.newSingleThreadScheduledExecutor();
            Queue<Packet> unconfirmed = new ArrayDeque<>();
            int packetNumber = packetsToSend.size();
            Runnable regularSendTask = () -> {
                if (!packetsToSend.isEmpty()) {
                    for (int i = 0; i < StaticDefinition.WINDOW_SIZE.value; i++) {
                        Packet packet = packetsToSend.poll();
                        if (packet == null) {
                            break;
                        }
                        unconfirmed.add(packet);
                        packetSender.sendPacket(packet, context.getRemoteIp(), context.getRemotePort());
                    }

                }
            };

            Runnable periodicResendTask = () -> {
                for (Packet packet : unconfirmed) {
                    if (confirmed.contains(PacketUtils.bytesToInt(packet.getSequenceNumber()))) {
                        unconfirmed.remove(packet);
                    } else {
                        packetsToSend.add(packet);
                    }
                }
                if (confirmed.size() == packetNumber) {
                    periodicResender.shutdown();
                }
            };


            regularSender.scheduleAtFixedRate(regularSendTask, 0, 100, TimeUnit.MILLISECONDS);
            periodicResender.scheduleAtFixedRate(periodicResendTask, 200, 200, TimeUnit.MILLISECONDS);

            while (!confirmed.containsAll(packetsToSend.stream().map(packet -> PacketUtils.bytesToInt(packet.getSequenceNumber())).toList())) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            packetsToSend.stream().map(packet -> PacketUtils.bytesToInt(packet.getSequenceNumber())).toList().forEach(confirmed::remove);

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
            }
        }).start();
    }


    private int localMessageId = 0;
    private Queue<Packet> generatePacketList(String message) {
        byte[] byteMessage = message.getBytes();
        List<byte[]> byteMessages = new ArrayList<>();
        if (byteMessage.length > StaticDefinition.MESSAGE_MAX_SIZE.value) {
            int i = 0;
            while (i < byteMessage.length) {
                byte[] messagePart = Arrays.copyOfRange(byteMessage, i, Math.min(i + StaticDefinition.MESSAGE_MAX_SIZE.value, byteMessage.length));
                byteMessages.add(messagePart);
                i += StaticDefinition.MESSAGE_MAX_SIZE.value;
            }
        } else {
            byteMessages.add(byteMessage);
        }
        Queue<Packet> list = new ArrayDeque<>();
        int numberOfPackets = byteMessages.size();
        for (int i = 0; i < numberOfPackets; i++) {
            PacketBuilder packetBuilder = new PacketBuilder();
            byte[] copy = Arrays.copyOf(sequenceNumber, sequenceNumber.length);
            packetBuilder.setSessionId(sessionId)
                    .setSequenceNumber(copy)
                    .setAckFlag((byte) 0)
                    .setPayloadType((byte) 0);
            String messageString = new String(byteMessages.get(i));
            String payload =
                    // language=JSON
                    """
                            {
                                "type": "simple",
                                "numberOfPackets": "%d",
                                "message": "%s",
                                "localMessageId": "%d",
                                "localMessageOffset": "%d"
                            }
                            """.formatted(numberOfPackets, messageString, localMessageId, i);
            byte[] payloadBytes = payload.getBytes();
            byte[] payloadLength = new byte[]{(byte) (payloadBytes.length >> 8), (byte) payloadBytes.length};
            packetBuilder.setPayloadLength(payloadLength)
                    .setPayload(payloadBytes);
            list.add(packetBuilder.build());

            sequenceNumber = PacketUtils.incrementSequenceNumber(sequenceNumber);
        }
        localMessageId++;
        return list;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isAck()) return;

        int sequenceNumber = PacketUtils.byteArrayToInt(packet.getSequenceNumber());
        confirmed.add(sequenceNumber);
        System.out.println("confirmed: " + confirmed);
    }
}
