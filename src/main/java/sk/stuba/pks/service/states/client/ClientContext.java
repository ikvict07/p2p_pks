package sk.stuba.pks.service.states.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import sk.stuba.pks.exception.SocketException;

@Service
public class ClientContext {
    private ClientState clientState;

    private final SendingSynState sendingSynState;
    private final SendingAckState sendingAckState;
    private final SendingRequestState sendingRequestState;

    @Setter
    @Getter
    private String serverAddress;
    @Setter
    @Getter
    private int serverPort;

    @Setter
    @Getter
    private byte[] sessionId = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    public ClientContext(SendingAckState sendingAckState, SendingRequestState sendingRequestState, SendingSynState sendingSynState) {
        this.clientState = sendingSynState;
        this.sendingAckState = sendingAckState;
        this.sendingRequestState = sendingRequestState;
        this.sendingSynState = sendingSynState;
    }


    public void setClientState(ClientStateType clientState) {
        switch (clientState) {
            case SENDING_SYN -> this.clientState = sendingSynState;
            case SENDING_REQUEST -> this.clientState = sendingRequestState;
            case SENDING_ACK -> this.clientState = sendingAckState;
        }
    }

    public void start(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        try {
            while (true) {
                clientState.start(this);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
