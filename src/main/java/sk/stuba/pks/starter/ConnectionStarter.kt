package sk.stuba.pks.starter

import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import sk.stuba.pks.library.enums.SocketConnectionType
import sk.stuba.pks.library.service.SocketConnection

@Component
class ConnectionStarter(
    private val applicationContext: ApplicationContext,
    private val connectionsState: ConnectionsState,
) {
    @EventListener(ContextRefreshedEvent::class)
    fun startConnections() {
        val connectors = applicationContext.getBeansOfType<SocketConnection>().values.filter { it.type == SocketConnectionType.CONNECTOR }
        println("Number of connectors: ${connectors.size}")
        connectors.forEach { connector ->
            println("Starting connector on port ${connector.port}")
            connector.initConnector(connector.getRemoteIp(), connector.remotePort)
            connectionsState.connections[connector.port] = (connector)
        }
    }
}
