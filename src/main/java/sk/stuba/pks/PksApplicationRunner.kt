package sk.stuba.pks

import lombok.extern.log4j.Log4j2
import org.reflections.Reflections.log
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import sk.stuba.pks.library.service.SocketConnection
import sk.stuba.pks.starter.configuration.SocketConfigurationProperties
import kotlin.system.exitProcess

@Component
@Log4j2
class PksApplicationRunner(
    private val connections: List<SocketConnection>,
    private val configurationProperties: SocketConfigurationProperties,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        log.info("Application started")

        var customSize = configurationProperties.maxPayloadSize

        while (true) {
            Thread.sleep(1000)
            val availableConnections =
                connections
                    .filter { !it.socket.isClosed.get() }
                    .filter { !it.socket.isClosedByMe() }
                    .map { it.getRemoteIp() + ":" + it.socket.serverPort }
            val notClosedConnections =
                connections.filter { !it.socket.isClosed.get() }

            if (notClosedConnections.isEmpty()) {
                println("No available connections left")
                exitProcess(0)
            }
            if (availableConnections.isNotEmpty()) {
                println(
                    "Write where you want to send message: $availableConnections",
                )
                println("Or write 'exit' to exit, change - to change max size of 1 packet")
                val command = readln()
                if (command == "exit") {
                    exitProcess(0)
                }
                if (command == "change") {
                    println("Write new max size of 1 packet (1 - 800):")
                    val size = readln().toLong()
                    customSize = size
                    continue
                }
                val connection =
                    connections.filter { !it.socket.isClosed.get() }.filter { !it.socket.isClosedByMe() }.find {
                        command ==
                            it.getRemoteIp() + ":" + it.socket.serverPort
                    }
                println(connection)
                connection?.run {
                    println(
                        "What to do? (message - to send message, file - to send file, close - to close connection, exit - to exit, corrupted - to send corrupted message)",
                    )
                    val message = readln()
                    when (message) {
                        "message" -> {
                            println("Write message:")
                            val message1 = readln()
                            connection.sendMessage(message1, customSize)
                        }

                        "file" -> {
                            println("Write file path:")
                            val filePath = readln()
                            connection.sendFile(filePath, customSize)
                        }

                        "close" -> {
                            connection.closeConnection()
                        }

                        "exit" -> {
                            exitProcess(0)
                        }

                        "corrupted" -> {
                            println("Write message:")
                            val message1 = readln()
                            connection.sendCorruptedMessage(message1, customSize)
                        }

                        else -> println("Unknown command")
                    }
                }
            }
        }
    }
}
