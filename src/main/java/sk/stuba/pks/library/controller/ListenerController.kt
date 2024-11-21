package sk.stuba.pks.library.controller

import org.springframework.stereotype.Component
import sk.stuba.pks.library.service.MessageListener
import sk.stuba.pks.ui.sendMessageGlobally
import java.nio.file.Files
import java.nio.file.Paths

@Component
class ListenerController : MessageListener {
    override fun onMessageReceive(
        message: String,
        connection: String?,
    ) {
        println("Received message: $message")
        if (connection != null) {
            sendMessageGlobally(connection, message)
        }
    }

    override fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
        connection: String?,
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
        println("Received file: $fileName")
        if (connection != null) {
            sendMessageGlobally(connection, "File $fileName received")
        }
    }
}
