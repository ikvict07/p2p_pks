package sk.stuba.pks

import lombok.extern.log4j.Log4j2
import org.reflections.Reflections.log
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import sk.stuba.pks.starter.SocketConnection


@Component
@Log4j2
class PksApplicationRunner (
    val connections: List<SocketConnection>
): ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        log.info("Application started")

        while (true) {
            Thread.sleep(1000)
            println("Write where you want to send message: ${connections.map { it.getRemoteIp() + ":" + it.port }}")
            val whereToConnect = readln()
            val connection = connections.find { whereToConnect == it.getRemoteIp() + ":" + it.port }
            println(connection)
            connection?.run {
                println("What message do you want to send? (message - to send message, file - to send file, exit - to exit)")
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
                    "exit" -> {}
                    else -> println("Unknown command")
                }
            }

        }
    }
}