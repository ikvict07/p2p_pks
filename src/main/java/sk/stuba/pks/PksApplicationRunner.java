package sk.stuba.pks;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sk.stuba.pks.annotation.DefaultOperationHandlerQualifier;
import sk.stuba.pks.service.operationHandler.OperationHandler;
import sk.stuba.pks.service.states.session.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Log4j2
@Component
public class PksApplicationRunner implements ApplicationRunner {

    private final OperationHandler operationHandler;
    private final Session session;

    @Value("${listenPort}")
    private int listenPort;

    @Autowired
    public PksApplicationRunner(@DefaultOperationHandlerQualifier OperationHandler operationHandler, Session session) {
        this.operationHandler = operationHandler;
        this.session = session;
    }

    @Override
    @Async
    public void run(ApplicationArguments args) {
        System.out.println("Listening on port: " + listenPort);
        System.out.println("What port send to?");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String input = reader.readLine();
            int remotePort = Integer.parseInt(input);
            session.createConnection("localhost", remotePort);
        } catch (IOException e) {
            log.error("Couldnt read from console");
        }
    }
}
