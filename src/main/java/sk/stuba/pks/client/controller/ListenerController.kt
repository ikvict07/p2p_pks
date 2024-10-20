package sk.stuba.pks.client.controller

import org.springframework.stereotype.Component
import sk.stuba.pks.library.MessageListener
import java.nio.file.Files
import java.nio.file.Paths

@Component
class ListenerController : MessageListener {
    override fun onMessageReceive(message: String) {
        println("Received message: $message")
    }

    override fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
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
    }
}
