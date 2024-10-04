package sk.stuba.pks.service.states.server;

import org.springframework.stereotype.Service;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.PacketSender;

@Service
public class WaitingForAckState implements ServerState {
    private final PacketSender packetSender;

    private final PacketReceiver packetReceiver;

    public WaitingForAckState(PacketSender packetSender, PacketReceiver packetReceiver) {
        this.packetSender = packetSender;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void start(ServerContext context) throws SocketException {
        packetReceiver.receive();
        context.setState(ServerStateType.WAITING_FOR_REQUEST);
    }
}
