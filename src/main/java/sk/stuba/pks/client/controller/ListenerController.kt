package sk.stuba.pks.client.controller

import sk.stuba.pks.library.MessageListener
import sk.stuba.pks.starter.ListenPort
import java.nio.file.Files
import java.nio.file.Paths

@ListenPort(port = "7081")
class ListenerController : MessageListener {
    override fun onMessageReceive(message: String) {
        println("Received message: $message")
    }

    override fun onFileReceive(fileName: String, fileContent: ByteArray) {
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
