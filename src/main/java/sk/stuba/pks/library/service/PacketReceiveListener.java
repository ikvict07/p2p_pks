package sk.stuba.pks.library.service;

import sk.stuba.pks.library.dto.Packet;

public interface PacketReceiveListener {
    void onPacketReceived(Packet packet);
}
