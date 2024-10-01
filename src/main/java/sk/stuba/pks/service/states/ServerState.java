package sk.stuba.pks.service.states;

import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.model.Message;

public interface ServerState {
    void start(ServerContext context) throws SocketException;
}