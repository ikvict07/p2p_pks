package sk.stuba.pks.old.service;

import sk.stuba.pks.old.dto.Packet;

public interface PacketReceiveListener {
    void onPacketReceived(Packet packet);
}
