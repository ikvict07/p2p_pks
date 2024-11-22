package sk.stuba.pks.library.controller

import org.reflections.Reflections.log
import org.springframework.stereotype.Component
import sk.stuba.pks.library.service.MessageListener
import sk.stuba.pks.ui.Printer
import sk.stuba.pks.ui.sendMessageGlobally
import java.nio.file.Files
import java.nio.file.Paths

@Component
class ListenerController : MessageListener {
    override fun onMessageReceive(
        message: String,
        connection: String,
    ) {
        log.info("Received message: $message")
        sendMessageGlobally(connection, message)
        Printer.printMessage(message, connection)
    }

    override fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
        connection: String,
    ) {
        val directory = Paths.get("src/main/resources")
        if (!Files.exists(directory)) {
            Files.createDirectories(directory)
        }

        val path = directory.resolve(fileName)
        if (!Files.exists(path)) {
            Files.createFile(path)
        }
        Files.write(path, fileContent)
        log.info("Received file: $fileName")
        sendMessageGlobally(connection, "File $fileName received")
        Printer.printFile(fileName, connection)
    }
}
