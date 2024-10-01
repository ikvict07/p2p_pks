package sk.stuba.pks.service;

import sk.stuba.pks.dto.Packet;

public interface Receiver {
    Packet receive();
}
