package sk.stuba.pks.starter

import org.reflections.Reflections.log
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import sk.stuba.pks.library.enums.SocketConnectionType
import sk.stuba.pks.library.service.SocketConnection

@Component
class ListeningStarter(
    private val applicationContext: ApplicationContext,
    private val connectionsState: ConnectionsState,
) {
    @EventListener(ContextRefreshedEvent::class)
    fun startConnections() {
        val listeners =
            applicationContext.getBeansOfType<SocketConnection>().values.filter { it.type == SocketConnectionType.LISTENER }
        log.info("Number of listeners: ${listeners.size}")
        listeners.forEach { listener ->
            log.info("Starting listener on port ${listener.port}")
            listener.initListener()
            connectionsState.connections[listener.port] = (listener)
        }
    }
}
