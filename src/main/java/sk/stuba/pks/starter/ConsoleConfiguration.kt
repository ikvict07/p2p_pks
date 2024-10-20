package sk.stuba.pks.starter

object ConsoleConfiguration {
    fun getListenersPorts(): List<String> {
        println("Do you want to start receiving messages? (y/n)")
        val input = readlnOrNull()
        if (input != "y") {
            return emptyList()
        }
        val ports = mutableListOf<String>()
        println("Enter the number of ports you want to listen to:")
        val n = readln().toInt()
        for (i in 1..n) {
            println("Enter the port number:")
            ports.add(readln())
        }
        return ports
    }

    fun getConnectTo(): List<ConnectToDto> {
        println("Do you want to connect to other client? (y/n)")
        val input2 = readln()
        if (input2 != "y") {
            return emptyList()
        }
        val connectTo = mutableListOf<ConnectToDto>()
        println("Enter the number of clients you want to connect to:")
        val n = readln().toInt()
        for (i in 1..n) {
            println("Enter the remote port number:")
            val port = readln()
            println("Enter the remote IP address:")
            val ip = readln()
            println("Enter your port:")
            val myPort = readln()
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
