package sk.stuba.pks.starter

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.stereotype.Component
import sk.stuba.pks.library.ConnectTo

@Component
class SocketConnectionBeanFactoryPostProcessor : BeanFactoryPostProcessor {

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val registry = beanFactory as BeanDefinitionRegistry

        val reflections = Reflections("sk.stuba.pks",Scanners.TypesAnnotated)

        val classesWithAnnotation = reflections.getTypesAnnotatedWith(ListenPort::class.java)

        for (clazz in classesWithAnnotation) {
            val listenPortAnnotation = clazz.getAnnotation(ListenPort::class.java)
            listenPortAnnotation?.let {
                val port = it.port
                registerSocketConnectorBean(registry, port)
            }
        }

        val connectToClasses = reflections.getTypesAnnotatedWith(ConnectTo::class.java)
        for (clazz in connectToClasses) {
            val connectToAnnotation = clazz.getAnnotation(ConnectTo::class.java)
            connectToAnnotation?.let {
                val port = it.myPort
                registerSocketConnectorBean(registry, port)
            }
        }
    }

    private fun registerSocketConnectorBean(registry: BeanDefinitionRegistry, port: String) {
        val beanDefinition = GenericBeanDefinition()
        beanDefinition.beanClass = SocketConnection::class.java
        beanDefinition.constructorArgumentValues = ConstructorArgumentValues().apply {
            addGenericArgumentValue(port)
        }
        println("Registering bean for port $port")
        registry.registerBeanDefinition("socketConnection$port", beanDefinition)
    }
}