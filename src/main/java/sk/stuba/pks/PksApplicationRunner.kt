package sk.stuba.pks

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import dev.nesk.akkurate.constraints.constrain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lombok.extern.log4j.Log4j2
import org.reflections.Reflections.log
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import sk.stuba.pks.library.service.SocketConnection
import sk.stuba.pks.library.util.Asker
import sk.stuba.pks.library.validators.validation.accessors.answer
import sk.stuba.pks.starter.ConnectionsState
import sk.stuba.pks.starter.configuration.SocketConfigurationProperties
import sk.stuba.pks.starter.configuration.UiConfigurationProperties
import sk.stuba.pks.ui.App
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.system.exitProcess

@Component
@Log4j2
class PksApplicationRunner(
    private val connections: MutableList<SocketConnection>,
    private val configurationProperties: SocketConfigurationProperties,
    private val connectionsState: ConnectionsState,
    private val uiConfigurationProperties: UiConfigurationProperties,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?): Unit =
        runBlocking {
            log.info("Application started")

            if (uiConfigurationProperties.enabled) {
                launch(Dispatchers.Default) {
                    application {
                        Window(onCloseRequest = ::exitApplication) {
                            App(connectionsState)
                        }
                    }
                }
            } else {
                runMainLoop(args)
            }
        }

    private fun runMainLoop(args: ApplicationArguments?) {
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
                val command =
                    Asker
                        .askWithOptions(
                            "Write where you want to send message: $availableConnections, or write 'exit' to exit, change - to change max size of 1 packet",
                            availableConnections + listOf("exit", "change"),
                        ).answer
                if (command == "exit") {
                    exitProcess(0)
                }
                if (command == "change") {
                    val size =
                        Asker
                            .ask("Write new max size of 1 packet (1 - 800): ") {
                                answer.isNotEmpty()
                                answer.isMatching("^[0-9]+$".toRegex())
                                answer.constrain {
                                    it.toInt() in 1..800
                                }
                            }.answer
                            .toLong()
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
                    val message =
                        Asker
                            .askWithOptions(
                                "What to do? (message - to send message, file - to send file, close - to close connection, back - to go back, corrupted - to send corrupted message)",
                                listOf("message", "file", "back", "corrupted"),
                            ).answer
                    when (message) {
                        "message" -> {
                            println("Write message:")
                            val message1 = readln()
                            connection.sendMessage(message1, customSize)
                        }

                        "file" -> {
                            val filePath =
                                Asker
                                    .ask("Write path to file: ", "Invalid path") {
                                        answer.isNotEmpty()
                                        answer.constrain {
                                            Files.exists(Path(it))
                                        }
                                    }.answer
                            connection.sendFile(filePath, customSize)
                        }

                        "back" -> {
                            return
                        }

                        "close" -> {
                            connection.closeConnection()
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
