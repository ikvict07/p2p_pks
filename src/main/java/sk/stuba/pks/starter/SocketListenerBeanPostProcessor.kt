package sk.stuba.pks.starter

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SocketListenerBeanPostProcessor: BeanPostProcessor, ApplicationContextAware {
    private val portToBeans = mutableMapOf<String, MutableList<MessageListener>>()
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    // В данном методе мы перехватываем бины, которые реализуют MessageListener и проверяем наличие аннотации @ListenPort
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is MessageListener) {
            val listenPortAnnotation = bean.javaClass.getAnnotation(ListenPort::class.java)
            if (listenPortAnnotation != null) {
                val port = listenPortAnnotation.port
                val listenersForPort = portToBeans.computeIfAbsent(port) { mutableListOf() }
                listenersForPort.add(bean)
            }
        }
        return bean
    }


    @EventListener(ContextRefreshedEvent::class)
    fun onApplicationEvent() {
        val beanFactory = applicationContext.autowireCapableBeanFactory as ConfigurableListableBeanFactory


        portToBeans.forEach { (port, listeners) ->
            val socketListener = SocketListener(port, listeners)
            beanFactory.registerSingleton("socketListener$port", socketListener)


            socketListener.init()
        }
    }
}