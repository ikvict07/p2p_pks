package sk.stuba.pks.starter

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import sk.stuba.pks.library.ConnectTo
import sk.stuba.pks.library.InjectMassageListeners
import sk.stuba.pks.library.MessageListener


@Component
class InjectMessageListenersAnnotationBeanPostProcessor : BeanPostProcessor {
    private val portToBeans = mutableMapOf<String, MutableList<MessageListener>>()
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean is MessageListener) {
            val listenPortAnnotation = bean.javaClass.getAnnotation(ListenPort::class.java)
            if (listenPortAnnotation != null) {
                val port = listenPortAnnotation.port
                val listenersForPort = portToBeans.computeIfAbsent(port) { mutableListOf() }
                listenersForPort.add(bean)
            }
            val connectToAnnotation = bean.javaClass.getAnnotation(ConnectTo::class.java)
            if (connectToAnnotation != null) {
                val port = connectToAnnotation.myPort
                val listenersForPort = portToBeans.computeIfAbsent(port) { mutableListOf() }
                listenersForPort.add(bean)
            }
        }
        if (bean is SocketConnection) {
            try {
                val socketListenerClass = bean::class.java
                val portField = socketListenerClass.getDeclaredField("port")
                portField.isAccessible = true
                val port = portField[bean] as String

                val listeners = portToBeans[port] ?: return bean
                socketListenerClass.declaredFields.forEach { field ->
                    field.annotations.forEach { annotation ->
                        if (annotation is InjectMassageListeners) {
                            field.isAccessible = true
                            field[bean] = listeners
                        }
                    }
                }
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return bean
    }

}