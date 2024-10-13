package sk.stuba.pks.old.service.states.server;

import sk.stuba.pks.old.exception.SocketException;

public interface ServerState {
    void start(ServerContext context) throws SocketException;
}