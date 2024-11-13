package sk.stuba.pks.starter

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
) {
    @EventListener(ContextRefreshedEvent::class)
    fun startConnections() {
        val listeners = applicationContext.getBeansOfType<SocketConnection>().values.filter { it.type == SocketConnectionType.LISTENER }
        println("Number of listeners: ${listeners.size}")
        listeners.forEach { listener ->
            println("Starting listener on port ${listener.port}")
            listener.initListener()
        }
    }
}
