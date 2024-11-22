package sk.stuba.pks.starter

import org.springframework.stereotype.Component
import sk.stuba.pks.library.service.SocketConnection

@Component
class ConnectionsState {
    val connections = mutableMapOf<String, SocketConnection>()
}
