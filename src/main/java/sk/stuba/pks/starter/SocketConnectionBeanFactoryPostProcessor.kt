package sk.stuba.pks.starter

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import sk.stuba.pks.library.enums.SocketConnectionType
import sk.stuba.pks.library.service.SocketConnection
import sk.stuba.pks.library.util.ConsoleConfiguration

@Component
class SocketConnectionBeanFactoryPostProcessor :
    BeanFactoryPostProcessor,
    EnvironmentAware {
    var isUiEnabled: Boolean = false

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val registry = beanFactory as BeanDefinitionRegistry

        val listeners = ConsoleConfiguration.getListenersPorts(isUiEnabled)
        println("Will listen on ${listeners.size} ports")
        for (port in listeners) {
            registerSocketConnectorBean(registry, port)
        }

        val connectors = ConsoleConfiguration.getConnectTo(isUiEnabled)
        println("Will connect to ${connectors.size} servers")
        for (connector in connectors) {
            registerSocketConnectorBean(registry, connector.myPort, connector.ip, connector.port.toInt())
        }
    }

    private fun registerSocketConnectorBean(
        registry: BeanDefinitionRegistry,
        port: String,
        remoteIp: String? = null,
        remotePort: Int? = null,
    ) {
        val beanDefinition = GenericBeanDefinition()
        beanDefinition.beanClass = SocketConnection::class.java

        if (remoteIp != null && remotePort != null) {
            beanDefinition.constructorArgumentValues =
                ConstructorArgumentValues().apply {
                    addGenericArgumentValue(port.toInt())
                    addGenericArgumentValue(remoteIp)
                    addGenericArgumentValue(remotePort)
                    addGenericArgumentValue(SocketConnectionType.CONNECTOR)
                }
        } else {
            beanDefinition.constructorArgumentValues =
                ConstructorArgumentValues().apply {
                    addGenericArgumentValue(port)
                    addGenericArgumentValue(SocketConnectionType.LISTENER)
                }
        }

        println("Registering bean for port $port")
        registry.registerBeanDefinition("socketConnection$port", beanDefinition)
    }

    override fun setEnvironment(environment: Environment) {
        isUiEnabled = environment.getProperty("ui.enabled").toBoolean()
    }
}
