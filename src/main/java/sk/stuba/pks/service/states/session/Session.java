package sk.stuba.pks.service.states.session;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.stuba.pks.service.PacketReceiver;
import sk.stuba.pks.util.IpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Component
@Data
public class Session {
    private byte[] sessionId;

    @Autowired
    private Context context;

    @Autowired
    private KeepAliveService keepAliveService;

    @Autowired
    private AckKeepAliveService ackKeepAliveService;

    @Autowired
    private PacketReceiver packetReceiver;

    private final List<Executor> tasks = new ArrayList<>();


    public void createConnection(String remoteIp, int remotePort) {
        context.setMyIp(IpUtil.getIp());
        context.setRemoteIp(remoteIp);
        context.setRemotePort(remotePort);
        sessionId = new byte[]{1, 1, 1, 1};

        packetReceiver.addListener(keepAliveService);
        packetReceiver.addListener(ackKeepAliveService);
        tasks.add(packetReceiver.startReceivingPackets());
        tasks.add(keepAliveService.startKeepAliveTask(this));
    }
}
