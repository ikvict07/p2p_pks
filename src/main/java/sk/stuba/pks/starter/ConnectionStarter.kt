package sk.stuba.pks.starter

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import sk.stuba.pks.library.ConnectTo

@Component
class ConnectionStarter(
    private val applicationContext: ApplicationContext,
) : BeanPostProcessor {
    private val connectorToServerAndPort = mutableMapOf<SocketConnection, Pair<String, Int>>()

    override fun postProcessBeforeInitialization(
        bean: Any,
        beanName: String,
    ): Any {
        bean.javaClass.declaredAnnotations.forEach {
            if (it is ConnectTo) {
                val port = it.myPort
                connectorToServerAndPort[applicationContext.getBean("socketConnection$port") as SocketConnection] =
                    it.address to it.port.toInt()
            }
        }
        return bean
    }

    @EventListener(ContextRefreshedEvent::class)
    fun startConnections() {
        connectorToServerAndPort.forEach { socket, pair ->
            println("Starting connection on port ${socket.port}")
            socket.initConnector(pair.first, pair.second)
        }
    }
}
