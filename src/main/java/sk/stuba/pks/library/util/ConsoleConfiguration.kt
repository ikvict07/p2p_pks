package sk.stuba.pks.library.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import sk.stuba.pks.library.validators.ipValidator
import sk.stuba.pks.library.validators.numberValidator
import sk.stuba.pks.library.validators.yesNoValidator
import sk.stuba.pks.ui.configuration.ConnectToScreen
import sk.stuba.pks.ui.configuration.ListenerPortsScreen

object ConsoleConfiguration {
    fun getListenersPorts(isInterface: Boolean = false): List<String> =
        if (isInterface) {
            getListenerPortsInt()
        } else {
            getListenersPortsNoInt()
        }

    fun getConnectTo(isInterface: Boolean = false): List<ConnectToDto> =
        if (isInterface) {
            getConnectToInt()
        } else {
            getConnectToNoInt()
        }

    private fun getConnectToNoInt(): List<ConnectToDto> {
        when (Asker.ask("Do you want to connect to other client? (y/n)", yesNoValidator).answer) {
            "n" -> return emptyList()
        }
        val connectTo = mutableListOf<ConnectToDto>()
        val n = Asker.ask("Enter the number of clients you want to connect to:", numberValidator).answer.toInt()
        for (i in 1..n) {
            val port = Asker.ask("Enter remote port: ", numberValidator).answer
            val ip = Asker.ask("Enter remote IP address: ", ipValidator).answer
            val myPort = Asker.ask("Enter your port: ", numberValidator).answer
            connectTo.add(ConnectToDto(port, ip, myPort))
        }
        return connectTo
    }

    private fun getConnectToInt(): List<ConnectToDto> {
        val connectTo = mutableListOf<ConnectToDto>()
        runBlocking {
            application(exitProcessOnExit = false) {
                var isOpen by remember { mutableStateOf(true) }

                if (isOpen) {
                    Window(onCloseRequest = { isOpen = false }) {
                        ConnectToScreen(connectTo) {
                            isOpen = false
                        }
                    }
                }
            }
        }
        return connectTo
    }

    private fun getListenersPortsNoInt(): List<String> {
        when (Asker.ask("Do you want to listen to any ports? (y/n)", yesNoValidator).answer) {
            "n" -> return emptyList()
        }
        val ports = mutableListOf<String>()

        val n = Asker.ask("Enter the number of ports you want to listen to:", numberValidator).answer.toInt()
        for (i in 1..n) {
            ports.add(Asker.ask("Enter the port:", numberValidator).answer)
        }
        return ports
    }

    private fun getListenerPortsInt(): List<String> {
        val ports = mutableListOf<String>()
        runBlocking {
            application(exitProcessOnExit = false) {
                var isOpen by remember { mutableStateOf(true) }

                if (isOpen) {
                    Window(onCloseRequest = { isOpen = false }) {
                        ListenerPortsScreen(ports) {
                            isOpen = false
                        }
                    }
                }
            }
        }
        return ports
    }
}

data class ConnectToDto(
    val port: String,
    val ip: String,
    val myPort: String,
)
