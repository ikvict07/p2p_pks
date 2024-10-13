package sk.stuba.pks.old.service;

import sk.stuba.pks.old.dto.Packet;

public interface Sender {
    void sendPacket(Packet packet, String serverAddress, int serverPort);
}
