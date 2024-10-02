package sk.stuba.pks.service.states.server;

import sk.stuba.pks.exception.SocketException;

public interface ServerState {
    void start(ServerContext context) throws SocketException;
}