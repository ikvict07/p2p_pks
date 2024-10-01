package sk.stuba.pks.service.operationHandler;

import org.springframework.stereotype.Service;
import sk.stuba.pks.dto.Packet;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.service.states.ServerContext;

@Service
public class ServerOperationHandler implements OperationHandler {

    private final ServerContext serverContext;
    private final PacketReceiver packetReceiver;
    public ServerOperationHandler(ServerContext serverContext, PacketReceiver packetReceiver) {
        this.serverContext = serverContext;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void handle(String operation) {
        if (operation.equals("server")) {
            Packet packet = packetReceiver.receive();
            serverContext.startServer();
        }
    }
}
