package sk.stuba.pks.service;

import sk.stuba.pks.dto.Packet;

public interface PacketReceiveListener {
    void onPacketReceived(Packet packet);
}
