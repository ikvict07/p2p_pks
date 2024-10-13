package sk.stuba.pks.old.service.operationHandler;

import org.springframework.stereotype.Service;

@Service
public class AskOperationHandler implements OperationHandler {
    public AskOperationHandler() {
    }

    @Override
    public void handle(String operation) {
        if (operation.equals("ask")) {
            System.out.println("Asking...");
        }
    }
}
