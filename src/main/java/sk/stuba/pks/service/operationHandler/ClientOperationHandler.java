package sk.stuba.pks.service.operationHandler;

import org.springframework.stereotype.Service;
import sk.stuba.pks.service.states.client.ClientContext;

import java.io.BufferedReader;/**/

@Service
public class ClientOperationHandler implements OperationHandler {

    private final ClientContext clientContext;

    public ClientOperationHandler(ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public void handle(String operation) {
        if (!operation.equals("client")) return;

        System.out.println("What is the IP address of the server?");
        String serverAddress;
        int serverPort;
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(System.in))) {
            serverAddress = reader.readLine();
            System.out.println("What is the port of the server?");
            serverPort = Integer.parseInt(reader.readLine());
            System.out.println("Client started");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        clientContext.start(serverAddress, serverPort);
    }
}
