package sk.stuba.pks.service.states.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.dto.PacketBuilder;
import sk.stuba.pks.model.FileMessage;
import sk.stuba.pks.model.Message;
import sk.stuba.pks.service.PacketReceiveListener;
import sk.stuba.pks.service.PacketSender;
import sk.stuba.pks.service.mapping.JsonService;
import sk.stuba.pks.util.PacketUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FileMessageListenerService implements PacketReceiveListener {
    @Autowired
    private PacketSender packetSender;

    @Autowired
    private Context context;

    private Map<Integer, Set<FileMessage>> messages = new HashMap<>();

    @Override
    public void onPacketReceived(Packet packet) {
        if (!packet.isData()) return;
        if (packet.isCorrupt()) return;
        if (PacketUtils.bytesToInt(packet.getPayloadLength()) == 0) return;

        Message message = JsonService.fromPayload(packet.getPayload());
        if (!(message instanceof FileMessage fileMessage)) return;

        System.out.println("Total packets expect: " + fileMessage.getNumberOfPackets());
        System.out.println("Received packet: " + fileMessage.getLocalMessageOffset());
        if (!messages.containsKey(fileMessage.getLocalMessageId())) {
            var set = new HashSet<FileMessage>();
            set.add(fileMessage);
            messages.put(fileMessage.getLocalMessageId(), set);
        } else {
            messages.get(fileMessage.getLocalMessageId()).add(fileMessage);
        }
        if (messages.get(fileMessage.getLocalMessageId()).size() == fileMessage.getNumberOfPackets()) {
            System.out.println("Creating file");
            createFile(fileMessage.getLocalMessageId());
        }

        packetSender.sendPacket(PacketBuilder.ackPacket(packet.getSessionId(), packet.getSequenceNumber()), context.getRemoteIp(), context.getRemotePort());
    }

    private void createFile(int localMessageId) {
        Set<FileMessage> messageSet = messages.get(localMessageId);

        System.out.println("Creating file from " + messageSet.size() + " packets");
        List<FileMessage> completeMessage = messageSet.stream()
                .sorted(Comparator.comparingInt(FileMessage::getLocalMessageOffset))
                .toList();
        System.out.println("Complete message: " + completeMessage.size());

        byte[] concatenatedBytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (FileMessage fileMessage : completeMessage) {
                outputStream.write(fileMessage.getPayload());
            }
            concatenatedBytes = outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while concatenating byte arrays", e);
        }

        System.out.println("Concatenated bytes: " + concatenatedBytes.length);
        String colorReset = "\u001B[0m";
        String colorYellow = "\u001B[33m";
        String colorGreen = "\u001B[32m";

        System.out.println(colorYellow + "========================================" + colorReset);
        System.out.println(colorGreen + "You have received a file: " + completeMessage.get(0).getFileName() + colorReset);
        System.out.println(colorYellow + "========================================" + colorReset);
        try {
            Path filePath = Path.of("/Users/antonhorobets/IdeaProjects/pks/src/main/resources", completeMessage.get(0).getFileName());
            Files.write(filePath, concatenatedBytes);
        } catch (IOException e) {
            System.out.println("Error while creating file");
            throw new RuntimeException(e);
        }
        messages.remove(completeMessage.get(0).getLocalMessageId());


    }
}
