package sk.stuba.pks.service.operationHandler;

import org.springframework.stereotype.Service;
import sk.stuba.pks.service.states.ServerContext;

@Service
public class ServerOperationHandler implements OperationHandler {

    private final ServerContext serverContext;

    public ServerOperationHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void handle(String operation) {
        if (operation.equals("server")) {
            System.out.println("Server started");
            serverContext.startServer();
        }
    }
}
