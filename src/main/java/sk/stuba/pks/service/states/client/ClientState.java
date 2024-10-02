package sk.stuba.pks.service.states.client;

import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.service.states.server.ServerContext;

public interface ClientState {
    void start(ClientContext context) throws SocketException;

}
