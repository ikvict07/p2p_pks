package sk.stuba.pks

import lombok.extern.log4j.Log4j2
import org.reflections.Reflections.log
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import sk.stuba.pks.starter.SocketConnection
import kotlin.system.exitProcess

@Component
@Log4j2
class PksApplicationRunner(
    private val connections: List<SocketConnection>,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        log.info("Application started")

        while (true) {
            Thread.sleep(1000)
            val availableConnections =
                connections
                    .filter { !it.socket.isClosed.get() }
                    .map { it.getRemoteIp() + ":" + it.socket.serverPort }
            println(
                "Write where you want to send message: $availableConnections",
            )
            if (availableConnections.isEmpty()) {
                println("No available connections left")
                exitProcess(0)
            }
            val whereToConnect = readln()
            val connection =
                connections.filter { !it.socket.isClosed.get() }.find {
                    whereToConnect ==
                        it.getRemoteIp() + ":" + it.socket.serverPort
                }
            println(connection)
            connection?.run {
                println(
                    "What message do you want to send? (message - to send message, file - to send file, close - to close connection, exit - to exit)",
                )
                val message = readln()
                when (message) {
                    "message" -> {
                        println("Write message:")
                        val message1 = readln()
                        connection.sendMessage(message1)
                    }

                    "file" -> {
                        println("Write file path:")
                        val filePath = readln()
                        connection.sendFile(filePath)
                    }

                    "close" -> {
                        connection.closeConnection()
                    }

                    "exit" -> {
                        exitProcess(0)
                    }

                    else -> println("Unknown command")
                }
            }
        }
    }
}
