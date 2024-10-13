package sk.stuba.pks.old.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.dto.Packet;
import sk.stuba.pks.old.dto.PacketBuilder;
import sk.stuba.pks.old.model.Message;
import sk.stuba.pks.old.model.SimpleMessage;
import sk.stuba.pks.old.service.PacketReceiveListener;
import sk.stuba.pks.old.service.PacketSender;
import sk.stuba.pks.old.service.mapping.JsonService;
import sk.stuba.pks.old.util.PacketUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MessageListenerService implements PacketReceiveListener {

    @Autowired
    private PacketSender packetSender;

    @Autowired
    private Context context;

    private Map<Integer, Set<SimpleMessage>> messages = new HashMap<>();

    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isData()) return;
        if (packet.isCorrupt()) return;
        if (PacketUtils.bytesToInt(packet.getPayloadLength()) == 0) return;

        Message message = JsonService.fromPayload(packet.getPayload());
        if (!(message instanceof SimpleMessage simpleMessage)) return;


        if (!messages.containsKey(simpleMessage.getLocalMessageId())) {
            var set = new HashSet<SimpleMessage>();
            set.add(simpleMessage);
            messages.put(simpleMessage.getLocalMessageId(), set);
        } else {
            messages.get(simpleMessage.getLocalMessageId()).add(simpleMessage);
        }
        printMessage();

        packetSender.sendPacket(PacketBuilder.ackPacket(packet.getSessionId(), packet.getSequenceNumber()), context.getRemoteIp(), context.getRemotePort());
    }

    private void printMessage() {
        for (Set<SimpleMessage> messageSet : messages.values()) {
            if (messageSet.size() == messageSet.stream().findFirst().get().getNumberOfPackets()) {
                List<SimpleMessage> completeMessage = messageSet.stream()
                        .sorted((m1, m2) -> Integer.compare(m1.getLocalMessageOffset(), m2.getLocalMessageOffset()))
                        .toList();

                byte[] concatenatedBytes = completeMessage.stream()
                        .map(SimpleMessage::getMessage)
                        .map(String::getBytes)
                        .reduce(new byte[0], (a, b) -> {
                            byte[] result = new byte[a.length + b.length];
                            System.arraycopy(a, 0, result, 0, a.length);
                            System.arraycopy(b, 0, result, a.length, b.length);
                            return result;
                        });

                String colorReset = "\u001B[0m";
                String colorYellow = "\u001B[33m";
                String colorGreen = "\u001B[32m";
                String colorBlue = "\u001B[34m";

                System.out.println(colorYellow + "========================================" + colorReset);
                System.out.println(colorGreen + "You have received a message:" + colorReset);
                System.out.println(colorBlue + new String(concatenatedBytes) + colorReset);
                System.out.println(colorYellow + "========================================" + colorReset);

                messages.remove(completeMessage.getFirst().getLocalMessageId());
            }
        }
    }
}
