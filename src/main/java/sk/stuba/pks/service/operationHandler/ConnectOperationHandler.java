package sk.stuba.pks.service.operationHandler;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.service.states.session.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Log4j2
@Service
public class ConnectOperationHandler implements OperationHandler {
    @Autowired
    private Session session;
    @Override
    public void handle(String operation) {
        if (!operation.equals("connect")) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter IP address: ");
            String ip = reader.readLine();
            System.out.println("Enter port: ");
            int port = Integer.parseInt(reader.readLine());
            System.out.println("Connecting to " + ip + ":" + port);
            session.createConnection(ip, port);

            String op = "";
            while (!op.equals("exit")) {
                op = reader.readLine();
                switch (op) {
                    case "message" -> {
                        System.out.println("Enter message: ");
                        String message = reader.readLine();
                        session.sendMessage(message);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Couldnt read from console");
        }
    }
}
