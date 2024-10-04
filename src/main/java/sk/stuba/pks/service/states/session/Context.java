package sk.stuba.pks.service.states.session;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Context {
    private String myIp;
    @Value("${listenPort}")
    private int myPort;
    private String remoteIp;
    private int remotePort;
}
