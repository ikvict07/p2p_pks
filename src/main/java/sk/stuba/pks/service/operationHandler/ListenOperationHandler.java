package sk.stuba.pks.service.operationHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.service.states.session.Session;

@Service
public class ListenOperationHandler implements OperationHandler {

    @Autowired
    private Session session;

    @Override
    public void handle(String operation) {
        if (!operation.equals("listen")) return;

        System.out.println("Listening started");
        session.listen();
    }
}
