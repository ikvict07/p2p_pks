package sk.stuba.pks.service.states;

import org.springframework.stereotype.Service;
import sk.stuba.pks.exception.SocketException;
import sk.stuba.pks.model.Message;

@Service
public class WaitingForAckState implements ServerState{

    @Override
    public void start(ServerContext context) throws SocketException {
        context.setState(ServerStateType.WAITING_FOR_REQUEST);
    }
}
