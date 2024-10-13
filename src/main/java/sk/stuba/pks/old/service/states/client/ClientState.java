package sk.stuba.pks.old.service.states.client;

import sk.stuba.pks.old.exception.SocketException;

public interface ClientState {
    void start(ClientContext context) throws SocketException;

}
