package sk.stuba.pks.service.states;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import sk.stuba.pks.exception.SocketException;

@Service
public class ServerContext {
    private ServerState state;

    private final WaitingForSynState waitingForSynState;
    private final WaitingForAckState waitingForAckState;
    private final WaitingForRequestState waitingForRequestState;

    @Getter
    @Setter
    private String clientAddress;
    @Getter
    @Setter
    private int clientPort;

    public ServerContext(WaitingForSynState waitingForSynState, WaitingForAckState waitingForAckState, WaitingForRequestState waitingForRequestState) {
        state = waitingForSynState;
        this.waitingForSynState = waitingForSynState;
        this.waitingForAckState = waitingForAckState;
        this.waitingForRequestState = waitingForRequestState;
    }

    public void setState(ServerStateType state) {
        switch (state) {
            case WAITING_FOR_SYN -> this.state = waitingForSynState;
            case WAITING_FOR_ACK -> this.state = waitingForAckState;
            case WAITING_FOR_REQUEST -> this.state = waitingForRequestState;
        }
    }

    public void startServer() {
        while (true) {
            try {
                state.start(this);
            } catch (SocketException e) {
                e.printStackTrace();
                break;
            }
        }
    }


}
