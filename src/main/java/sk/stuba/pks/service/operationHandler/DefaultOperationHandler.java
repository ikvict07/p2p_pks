package sk.stuba.pks.service.operationHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.annotation.DefaultOperationHandlerQualifier;

import java.util.List;

@Service
@DefaultOperationHandlerQualifier
public class DefaultOperationHandler implements OperationHandler {
    private final List<OperationHandler> operationHandlers;
    @Autowired
    public DefaultOperationHandler(List<OperationHandler> operationHandlers) {
        this.operationHandlers = operationHandlers;
    }

    @Override
    public void handle(String operation) {
        for (OperationHandler operationHandler : operationHandlers) {
            operationHandler.handle(operation);
        }
    }
}
