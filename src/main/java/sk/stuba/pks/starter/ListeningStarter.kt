package sk.stuba.pks.starter

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import sk.stuba.pks.library.ConnectTo

@Component
class ListeningStarter(
    private val applicationContext: ApplicationContext,
) : BeanPostProcessor {
    private val listeners = mutableListOf<SocketConnection>()
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        bean.javaClass.declaredAnnotations.forEach {
            if (it is ListenPort) {
                val port = it.port
                listeners.add(applicationContext.getBean("socketConnection$port") as SocketConnection)
            }
        }
        return bean
    }

    @EventListener(ContextRefreshedEvent::class)
    fun startConnections() {
        listeners.forEach { listener ->
            println("Starting listener on port ${listener.port}")
            listener.initListener()
        }
    }
}