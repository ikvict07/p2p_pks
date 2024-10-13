package sk.stuba.pks;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sk.stuba.pks.old.annotation.DefaultOperationHandlerQualifier;
import sk.stuba.pks.old.service.ChatOperatingService;
import sk.stuba.pks.old.service.operationHandler.OperationHandler;
import sk.stuba.pks.old.service.states.session.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Log4j2
@Component
public class PksApplicationRunner implements ApplicationRunner {

    private final OperationHandler operationHandler;

    @Autowired
    private final Session session;
    private final ChatOperatingService chatOperatingService;
    @Value("${listenPort}")
    private int listenPort;

    @Autowired
    public PksApplicationRunner(@DefaultOperationHandlerQualifier OperationHandler operationHandler, Session session, ChatOperatingService chatOperatingService) {
        this.operationHandler = operationHandler;
        this.session = session;
        this.chatOperatingService = chatOperatingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Listening on port: " + listenPort);
        System.out.println("listen/connect?");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String operation = reader.readLine();
            operationHandler.handle(operation);

            while (true) {
                System.out.println("message/file/exit?");
                String op = reader.readLine();

                switch (op) {
                    case "message" -> {
                        System.out.println("Enter message: ");
                        String message = reader.readLine();
                        session.sendMessage(message);
                    }
                    case "file" -> {
                        System.out.println("Enter file path: ");
                        String filePath = reader.readLine();
                        session.sendFile(filePath);
                    }
                    case "exit" -> {
                        System.out.println("Exiting...");
                        return; // Exit the loop and terminate the application
                    }
                    default -> System.out.println("Unknown operation: " + op);
                }
            }
        } catch (IOException e) {
            log.error("Couldn't read from console", e);
        }
    }
}
