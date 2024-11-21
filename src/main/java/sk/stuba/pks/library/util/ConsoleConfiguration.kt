package sk.stuba.pks.library.util

import sk.stuba.pks.library.validators.ipValidator
import sk.stuba.pks.library.validators.numberValidator
import sk.stuba.pks.library.validators.yesNoValidator

object ConsoleConfiguration {
    fun getListenersPorts(): List<String> {
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

    fun getConnectTo(): List<ConnectToDto> {
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
}

data class ConnectToDto(
    val port: String,
    val ip: String,
    val myPort: String,
)
