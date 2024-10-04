package sk.stuba.pks.service;

import sk.stuba.pks.dto.Packet;

public interface Sender {
    void sendPacket(Packet packet, String serverAddress, int serverPort);
}
